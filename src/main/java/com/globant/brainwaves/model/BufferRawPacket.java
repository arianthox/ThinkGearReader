package com.globant.brainwaves.model;


import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;


public class BufferRawPacket implements Packet {

    private int[] bufferRawEeg;

    public BufferRawPacket(int[] bufferRawEeg) {
        this.bufferRawEeg = bufferRawEeg;
    }

    public int[] getBufferRawEeg() {
        return bufferRawEeg;
    }

    @Override
    public HashMap<String, Object> toHashMap() {
        HashMap<String,Object> map = new HashMap<>();
        map.put("BufferRawEeg",Arrays.toString(bufferRawEeg));
        return map;
    }
}
