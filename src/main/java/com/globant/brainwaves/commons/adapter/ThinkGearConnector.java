package com.globant.brainwaves.commons.adapter;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.japi.pf.PFBuilder;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.*;
import akka.util.ByteString;
import com.globant.brainwaves.ThinkGearReaderApplication;
import com.globant.brainwaves.client.CoreEngineClient;
import com.globant.brainwaves.commons.model.*;
import com.globant.brainwaves.model.EventListener;
import com.globant.brainwaves.utils.Extend;
import com.google.gson.Gson;
import lombok.extern.java.Log;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.function.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.globant.brainwaves.utils.Extend.isLike;
import static io.vavr.API.*;

@Log
@Component
public class ThinkGearConnector {
    
    private static Gson gson = new Gson();

    private static List<EventListener> listeners = new ArrayList<>();

    private static List<Integer> rawPacketBuffer = Collections.synchronizedList(new ArrayList<>());

    private static final int MAX_RAW_BUFFER_SIZE = 512;

    private static final Predicate<? super Packet> isRawPacket = packet -> packet instanceof RawPacket;

    private static Function<RawPacket, Boolean> bufferRawPacket = packet -> {
        rawPacketBuffer.add(packet.getRawEeg());
        return rawPacketBuffer.size() >= MAX_RAW_BUFFER_SIZE;
    };

    private static final BiPredicate<Function<RawPacket, Boolean>, RawPacket> isRawBufferReady = (rawPacketBooleanFunction, rawPacket) -> rawPacketBooleanFunction.apply(rawPacket);

    private static final Consumer<? super Packet> processConsumer = packet -> {
        log.log(Level.FINE, String.format("Event: %s  - %s",  packet.getClass().getName(), Collections.singletonList(packet.toHashMap()).toString()));
        listeners.forEach(eventListener -> eventListener.processPacket(packet));
    };

    private static final Function<List<Integer>, BufferRawPacket> newBufferRawPacket = buffer -> {
        BufferRawPacket packet = new BufferRawPacket(buffer.stream().mapToInt(value -> value.intValue()).toArray());
        rawPacketBuffer.clear();
        return packet;
    };

    private static Function<? super Packet, Optional<Packet>> mainFlatMapperPacket = packet ->
            Optional.of(
                    isRawPacket.test(packet) && isRawBufferReady.test(bufferRawPacket, (RawPacket) packet) ?
                            newBufferRawPacket.apply(rawPacketBuffer) :
                            packet
            );


    private static Predicate<String> isPossibleRawPacket = s -> convertToBinary(s, "big5").contains("000000ff");

    private static Consumer<String> unknownPacketConsumer = s -> {
        log.finer(String.format("UnknownEvent: %s", s));
    };

    private static Predicate<? super Class<? extends Packet>> unknownPacketPredicate = aClass -> aClass != UnknownPacket.class;

    private static Function<String, String> convertToRaw = s -> String.format("{raw=%s}", Arrays.toString(s.getBytes()));

    private static BiFunction<String, ? super Class<? extends Packet>, ? extends Packet> extractPacket = (s, aClass) -> ShortRawPacket.class.isAssignableFrom(aClass) ? gson.fromJson(convertToRaw.apply(s), aClass) : gson.fromJson(s, aClass);

    private transient String appName, sha_1;

    private transient ActorSystem system = ActorSystem.create();

    private transient Materializer materializer = ActorMaterializer.create(system);

    private transient Flow<ByteString, ByteString, CompletionStage<Tcp.OutgoingConnection>> outgoingConnection;

    @Value("${think-gear-connector.format}")
    private transient String format;

    @Value("${think-gear-connector.raw}")
    private transient boolean raw;

    @Value("${think-gear-connector.authMessage}")
    private transient String authMessage;

    @Value("${think-gear-connector.switchMessage}")
    private transient String switchMessage;

    @Value("${think-gear-connector.port}")
    private transient int port;

    @Value("${think-gear-connector.retries}")
    private transient int retries;

    @Value("${think-gear-connector.host}")
    private transient String host;

    private transient CoreEngineClient coreEngineClient;

    private final String sessionId=UUID.randomUUID().toString();


    private ThinkGearConnector(String appName, String SHA_1) {
        this.appName = appName;
        this.sha_1 = SHA_1;
    }

    @Autowired
    public ThinkGearConnector(CoreEngineClient coreEngineClient) {
        this(ThinkGearReaderApplication.class.getName(), DigestUtils.sha1Hex(ThinkGearReaderApplication.class.getName()));
        this.coreEngineClient = coreEngineClient;
    }

    @PostConstruct
    private void init() {
        Tcp tcp = Tcp.get(system);
        outgoingConnection = tcp.outgoingConnection(this.host, this.port);
        start();
        this.registerEventHandler(p -> {
            try {
                if (p instanceof BufferRawPacket) {
                    coreEngineClient.receive("ThinkGearReader",sessionId, (BufferRawPacket) p);
                }else if(p instanceof ChannelPacket){
                    coreEngineClient.receive("ThinkGearReader",sessionId, (ChannelPacket) p);
                }
            }catch (Exception ex){
                log.warning(ex.getMessage());
            }

        });
    }

    public void registerEventHandler(EventListener e) {
        this.listeners.add(e);
    }

    private void auth(Flow<ByteString, ByteString, CompletionStage<Tcp.OutgoingConnection>> connection) {
        this.writeJson(connection, String.format(authMessage, this.appName, this.sha_1));
    }

    private void switchOutput(Flow<ByteString, ByteString, CompletionStage<Tcp.OutgoingConnection>> connection, boolean enableRawOutput, String format) {
        this.writeJson(connection, String.format(switchMessage, enableRawOutput, format));
    }

    private void start() {
        try {
            log.info("Starting ThinkGear Connector");
            this.auth(outgoingConnection);
            switchOutput(outgoingConnection, raw, format);

        } catch (Exception e) {
            log.severe(e.getMessage());
        }

    }


    private void writeJson(Flow<ByteString, ByteString, CompletionStage<Tcp.OutgoingConnection>> connection, String json) {

        log.fine(String.format("write: %s", json));

        Source<ByteString, NotUsed> source = Source.single(json).map(i -> ByteString.fromString(json));
        Source<ByteString, NotUsed> reply = source.via(connection);

        reply
                .recoverWithRetries(retries,
                        new PFBuilder().match(RuntimeException.class, ex -> source).build()
                )
                .toMat(Sink.foreach(ThinkGearConnector::process), Keep.right())
                .run(materializer);

    }

    private static String convertToBinary(String input, String encoding) {
        byte[] encoded_input = Charset.forName(encoding)
                .encode(input)
                .array();
        return IntStream.range(0, encoded_input.length)
                .map(i -> encoded_input[i])
                .mapToObj(e -> Integer.toHexString(e ^ 255))
                .map(e -> String.format("%1$" + Byte.SIZE + "s", e).replace(" ", "0"))
                .collect(Collectors.joining(" "));
    }


    private static void process(ByteString x) {

        Optional.of(x).map(ByteString::utf8String).map(Scanner::new).map(Extend::streamScanner).filter(i -> i != null).ifPresent(stream ->
                stream.forEachOrdered(s ->
                        Optional.of(
                                Match(s).of(
                                        Case($(isLike("status")), StatusPacket.class),
                                        Case($(isLike("eSense")), ChannelPacket.class),
                                        Case($(isLike("blink")), BlinkPacket.class),
                                        Case($(isLike("mentalEffort")), MentalEffortPacket.class),
                                        Case($(isLike("familiarity")), FamiliarityPacket.class),
                                        Case($(isLike("raw")), RawPacket.class),
                                        Case($(), () -> {
                                            unknownPacketConsumer.accept(s);
                                            return UnknownPacket.class;
                                        })
                                )).filter(unknownPacketPredicate).map(aClass -> extractPacket.apply(s, aClass)).flatMap(mainFlatMapperPacket).ifPresent(processConsumer)
                )
        );

    }

}