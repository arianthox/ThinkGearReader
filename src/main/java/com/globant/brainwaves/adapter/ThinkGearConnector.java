package com.globant.brainwaves.adapter;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.*;
import akka.util.ByteString;
import com.globant.brainwaves.ThinkGearReaderApplication;
import com.globant.brainwaves.model.*;
import com.globant.brainwaves.utils.Extend;
import com.google.gson.Gson;
import io.vavr.API;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

import static com.globant.brainwaves.utils.Extend.isLike;
import static io.vavr.API.*;

@Component
public class ThinkGearConnector {

    private static boolean debug = false;

    private static Gson gson = new Gson();

    private String appName, sha_1;

    private static final Logger logger = Logger.getLogger(ThinkGearConnector.class.getName());

    private ActorSystem system = ActorSystem.create();

    private Materializer materializer = ActorMaterializer.create(system);

    private Flow<ByteString, ByteString, CompletionStage<Tcp.OutgoingConnection>> outgoingConnection;

    private Flow<ByteString, ByteString, CompletionStage<Tcp.OutgoingConnection>> rawOutgoingConnection;

    private static List<EventListener> listeners = new ArrayList<>();

    @Value("${thinkGearConnector.format}")
    private String format;

    @Value("${thinkGearConnector.raw}")
    private boolean raw;

    @Value("${thinkGearConnector.authMessage}")
    private String authMessage;

    @Value("${thinkGearConnector.switchMessage}")
    private String switchMessage;

    @Value("${thinkGearConnector.port}")
    private int port;

    @Value("${thinkGearConnector.host}")
    private String host;

    public ThinkGearConnector() {
        this(ThinkGearReaderApplication.class.getName(), DigestUtils.sha1Hex(ThinkGearReaderApplication.class.getName()));
    }

    private ThinkGearConnector(String appName, String SHA_1) {
        this.appName = appName;
        this.sha_1 = SHA_1;
    }

    @PostConstruct
    private void init() {
        Tcp tcp = Tcp.get(system);
        outgoingConnection = tcp.outgoingConnection(this.host, this.port);
        rawOutgoingConnection = tcp.outgoingConnection(this.host, this.port);
        start();
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

    public void start() {
        try {
            logger.info("Starting ThinkGear Connector");
            this.auth(outgoingConnection);
            switchOutput(outgoingConnection, false, format);

            if (raw) {
                this.auth(rawOutgoingConnection);
                switchOutput(rawOutgoingConnection, raw, format);
            }

        } catch (Exception e) {
            logger.severe(e.getMessage());
        }

    }


    private void writeJson(Flow<ByteString, ByteString, CompletionStage<Tcp.OutgoingConnection>> connection, String json) {

        logger.info(String.format("write: %s" , json));

        Source<ByteString, NotUsed> source = Source.single(json).map(i -> ByteString.fromString(json));
        Source<ByteString, NotUsed> reply = source.via(connection);
        reply.toMat(Sink.foreach(ThinkGearConnector::process), Keep.right()).run(materializer).whenComplete((success, failure) -> {
            if (failure != null) {
                logger.info(failure.getMessage());
            }
            system.terminate();
        });
    }


    private static void process(ByteString x) {

            Optional.of(x).map(ByteString::utf8String).map(Scanner::new).map(Extend::streamScanner).ifPresent(in -> {
                if (debug) logger.finer("Debug:" + in);

                in.filter(s -> s != null).forEach(s -> {
                    Optional<Class<? extends Packet>> classOptional = Optional.of(
                            Match(s).of(
                                    Case($(isLike("status")), StatusPacket.class),
                                    Case($(isLike("eSense")), ChannelPacket.class),
                                    Case($(isLike("blink")), BlinkPacket.class),
                                    Case($(isLike("mentalEffort")), MentalEffortPacket.class),
                                    Case($(isLike("familiarity")), FamiliarityPacket.class),
                                    Case($(isLike("raw")), RawPacket.class),
                                    Case(API.$(), UnknownPacket.class)
                            ));
                    classOptional.filter(aClass -> aClass != UnknownPacket.class).ifPresent(aClass -> {
                        try {
                            Packet packet = gson.fromJson(s, aClass);
                            listeners.forEach((p) -> p.processPacket(packet));
                        } catch (Exception ex) {
                            logger.warning(String.format("Exception:[%s] - [%s]", ex.getMessage(), s));
                        }
                    });
                });


            });

    }

}