package com.globant.brainwaves.model;

import java.util.HashMap;
import java.util.List;


public class ShortRawPacket implements Packet {

    private List<Short> raw;

    public List<Short> getRaw() {
        return raw;
    }

    @Override
    public HashMap<String, Object> toHashMap() {
        HashMap<String,Object> map = new HashMap<>();
        map.put("raw[]",this.raw);
        return map;
    }

    /*

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
     */
}
