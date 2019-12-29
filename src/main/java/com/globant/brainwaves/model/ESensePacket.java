package com.globant.brainwaves.model;



import java.util.HashMap;


public class ESensePacket implements Packet{

    private int attention;
    private int meditation;


    public ESensePacket setAttention(int attention) {
        this.attention = attention;
        return this;
    }

    public ESensePacket setMeditation(int meditation) {
        this.meditation = meditation;
        return this;
    }

    public int getAttention() {
        return attention;
    }

    public int getMeditation() {
        return meditation;
    }

    @Override
    public String toString() {
        return "Att"+getAttention();
    }

    @Override
    public HashMap<String, Object> toHashMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("attention",this.attention);
        map.put("meditation",this.meditation);
        return map;
    }
}
