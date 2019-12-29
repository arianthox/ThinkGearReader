package com.globant.brainwaves.adapter;

import com.globant.brainwaves.ThinkGearReaderApplication;
import com.globant.brainwaves.model.*;
import com.google.gson.Gson;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import io.vavr.API;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.microedition.io.StreamConnection;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InvalidObjectException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.globant.brainwaves.utils.Extend.isLike;
import static io.vavr.API.*;

@Component
public class ThinkGearConnector {


    private String address;
    private boolean debug = false;
    private Gson gson;
    private String appName, sha_1, host;
    private int port = 13854;
    private StreamThread stream;
    private CommPortIdentifier cport;
    private boolean hacked = false;
    private Logger logger = Logger.getLogger(ThinkGearConnector.class.getName());

    @Value("${thinkGearConnector.format}")
    private String format;

    @Value("${thinkGearConnector.raw}")
    private boolean raw;

    public ThinkGearConnector() {

    }


    @Bean
    public ThinkGearConnector getThinkGearConnector() throws IOException, org.springframework.boot.web.server.PortInUseException {
        ThinkGearConnector thinkGearConnector = null;
        try {
            thinkGearConnector = new ThinkGearConnector(ThinkGearReaderApplication.class.getName(), DigestUtils.sha1Hex(ThinkGearReaderApplication.class.getName()));
            try {
                thinkGearConnector.open();
            } catch (PortInUseException e) {
                logger.severe(e.getMessage());
            }
            thinkGearConnector.auth();
            thinkGearConnector.registerEventHandler(in -> System.out.println(Collections.singletonList(in.toHashMap())));
            Thread.sleep(1000);
            thinkGearConnector.enableDebug();
            thinkGearConnector.switchOutput(raw, format);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return thinkGearConnector;
    }

    /**
     * Used for hacked MindFlex headsets
     *
     * @param port The CommPort of the connected
     */

    public ThinkGearConnector(CommPortIdentifier port) {
        this.hacked = true;
        this.cport = port;
        gson = new Gson();

    }

    /**
     * Connect to the ThinkGearConnector application from NeuroSky
     *
     * @param appName Your Application name
     * @param SHA_1   the hash of your app name - must be unique
     */
    public ThinkGearConnector(String appName, String SHA_1) {
        this.appName = appName;
        this.sha_1 = SHA_1;
        this.host = "127.0.0.1";
        gson = new Gson();
    }


    /**
     * WIP - Will currently not work
     *
     * @param address
     */
    private ThinkGearConnector(String address) {
        this.address = address;
        gson = new Gson();
    }

    /**
     * Reset the host
     *
     * @param host the host
     */
    public void setHost(String host) {
        this.setHost(host, this.port);
    }

    /**
     * Reset the host
     *
     * @param host the host
     * @param port the port
     */
    public void setHost(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Opens the connection
     *
     * @throws IOException
     * @throws PortInUseException
     */
    public void open() throws IOException, PortInUseException {
        if (!hacked)
            this.stream = new StreamThread(new Socket(this.host, this.port));
        else if (address != null)
            this.stream = new StreamThread((StreamConnection) null); ///TODO ADD BLUETOOTH SUPPORT
        else this.stream = new StreamThread(cport.open("ThinkGearConnectorBridge", 1000));


        this.stream.start();

    }

    /**
     * prints debug information to the console
     */
    public void enableDebug() {
        debug = true;
    }

    /**
     * authenticate with the ThinkGearConnector from NeuroSky
     */
    public void auth() {
        this.stream.writeJson(String.format("{\"appName\":\"%s\",\"appKey\":\"%s\"}\n", this.appName, this.sha_1));
    }

    /**
     * @param enableRawOutput should raw data be send?
     * @param format          use 'json' to receive json data
     */
    public void switchOutput(boolean enableRawOutput, String format) {
        this.stream.writeJson(String.format("{\"enableRawOutput\":%s,\"format\":\"%s\"}\n", enableRawOutput, format));
    }

    /**
     * Registers an event handler for incoming packets
     *
     * @param e the event handler to be registered
     */
    public void registerEventHandler(EventListener e) {
        this.stream.listeners.add(e);
    }


    /**
     * Close the Connection
     */
    public void close() {
        this.stream.close();
    }

    public interface EventListener {
        /**
         * Process incomming packets
         *
         * @param in Incomming packet
         */
        public void processPacket(Packet in);
    }

    class StreamThread extends Thread {
        Scanner reader;
        OutputStreamWriter writer;
        boolean running = false;
        private StreamConnection stream;
        private CommPort port;
        private List<EventListener> listeners = new ArrayList<>();
        private Socket socket;

        private StreamThread(StreamConnection stream) {
            this.stream = stream;
            try {
                reader = new Scanner(new InputStreamReader(stream.openInputStream()));
                writer = new OutputStreamWriter(stream.openOutputStream());

            } catch (IOException e) {
                logger.severe(e.getMessage());
            }
        }

        private StreamThread(CommPort port) {
            this.port = port;
            try {
                reader = new Scanner(new InputStreamReader(port.getInputStream()));
                writer = new OutputStreamWriter(port.getOutputStream());

            } catch (IOException e) {
                logger.severe(e.getMessage());
            }
        }

        private StreamThread(Socket socket) {
            try {
                this.socket = socket;
                reader = new Scanner(new InputStreamReader(socket.getInputStream()));
                writer = new OutputStreamWriter(socket.getOutputStream());
            } catch (Exception e) {
                logger.severe(e.getMessage());
            }
        }

        @Override
        public void run() {
            this.running = true;
            if (!hacked) {
                while (this.running) {
                    if (socket.isClosed()) {
                        try {
                            throw new InvalidObjectException("Socket is closed");
                        } catch (InvalidObjectException e) {
                            logger.severe(e.getMessage());
                        }
                    }


                    try {
                        if (reader.hasNextLine()) {
                            Optional.of(reader.nextLine()).ifPresent(in -> {
                                if (debug) logger.finer("Debug:"+in);
                                Optional<Class<? extends Packet>> classOptional = Optional.of(
                                        Match(in).of(
                                                Case($(isLike("status")), StatusPacket.class),
                                                Case($(isLike("eSense")), ChannelPacket.class),
                                                Case($(isLike("blink")), BlinkPacket.class),
                                                Case($(isLike("mentalEffort")), MentalEffortPacket.class),
                                                Case($(isLike("familiarity")), FamiliarityPacket.class),
                                                Case($(isLike("raw")), RawPacket.class),
                                                Case(API.$(), UnknownPacket.class)
                                        ));
                                classOptional.ifPresent(aClass -> {
                                    listeners.forEach((p) -> p.processPacket(gson.fromJson(in, aClass)));
                                });
                            });
                        }
                    } catch (Exception e) {
                        logger.severe(e.getMessage());
                        continue;
                    }
                }
            } else if (address != null) {
                while (this.running) {
                    if (this.reader.hasNextByte()) {
                        if (this.reader.nextByte() == 170)
                            if (this.reader.nextByte() == 170) {
                                boolean bigPacket = false;

                                byte payloadLength = reader.nextByte();
                                if (payloadLength > 169)                      //Payload length can not be greater than 169
                                    return;

                                byte payloadData[] = new byte[64];
                                byte generatedChecksum = 0;
                                for (int i = 0; i < payloadLength; i++) {
                                    payloadData[i] = this.reader.nextByte();            //Read payload into memory
                                    generatedChecksum += payloadData[i];
                                }

                                byte checksum = this.reader.nextByte();                      //Read checksum byte from stream
                                generatedChecksum = (byte) (255 - generatedChecksum);   //Take one's compliment of generated checksum

                                if (checksum == generatedChecksum) {

                                    int poorQuality = 200;
                                    int attention = 0;
                                    int meditation = 0;

                                    for (int i = 0; i < payloadLength; i++) {    // Parse the payload

                                        switch (payloadData[i]) {
                                            case (byte) 1:
                                                i++;

                                                break;
                                            case (byte) 2:
                                                i++;
                                                poorQuality = payloadData[i];
                                                bigPacket = true;
                                                break;
                                            case (byte) 4:
                                                i++;
                                                attention = payloadData[i];
                                                break;
                                            case (byte) 5:
                                                i++;
                                                meditation = payloadData[i];
                                                break;
                                            case (byte) 0x80:
                                                i = i + 3;
                                                break;
                                            case (byte) 131:
                                                i = i + 25;
                                                break;
                                            default:
                                                break;
                                        } // switch
                                    } // for loop
                                }
                            }
                    }

                }
            } else while (this.running) {
                try {
                    if (reader.hasNextLine()) {
                        String in = reader.nextLine();
                        if (in.isEmpty()) continue;
                        if (debug) logger.finer(in);
                        String[] data = in.split(",");
                        ESensePacket sense = new ESensePacket();
                        sense.setAttention(Integer.valueOf(data[1])).setMeditation(Integer.valueOf(data[2]));
                        EEGPowerPacket power = new EEGPowerPacket();
                        power.setDelta(Long.valueOf(data[3])).setTheta(Long.parseLong(data[4])).setLowAlpha(Long.parseLong(data[5])).setHighAlpha(Long.parseLong(data[6])).setLowBeta(Long.parseLong(data[7])).setHighBeta(Long.parseLong(data[8])).setLowGamma(Long.parseLong(data[9])).setHighGamma(Long.parseLong(data[10]));
                        listeners.forEach((p) -> new ChannelPacket().setEegPower(power).seteSense(sense).setPoorSignalLevel(Integer.parseInt(data[0])));

                    }
                } catch (Exception e) {
                    logger.severe(e.getMessage());
                    continue;
                }
            }
            try {
                this.socket.close();
            } catch (IOException e) {
                logger.severe(e.getMessage());
            }
        }

        private void writeJson(String json) {
            Logger.getAnonymousLogger().log(Level.INFO, String.format("Writing: %s", json));
            try {
                writer.write(json);
                writer.flush();
            } catch (IOException e) {
                logger.severe(e.getMessage());
            }
        }

        private void close() {
            this.running = false;
        }


    }
}
