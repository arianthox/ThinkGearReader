package com.globant.brainwaves.model;

import java.util.HashMap;
import java.util.logging.Level;

/**
 * Created by root on 19.05.2017.
 */
public class RawPacket implements Packet {

    private int rawEeg;

    public Level getLogLevel(){
        return Level.FINEST;
    }

    public int getRawEeg() {
        return rawEeg;
    }

    @Override
    public HashMap<String, Object> toHashMap() {
        HashMap<String,Object> map = new HashMap<>();
        map.put("rawEeg",this.rawEeg);
        return map;
    }
}
