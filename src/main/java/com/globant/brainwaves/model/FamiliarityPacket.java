package com.globant.brainwaves.model;

import java.util.HashMap;


public class FamiliarityPacket implements Packet{

    private double familiarity;

    public double getFamiliarity() {
        return familiarity;
    }

    @Override
    public String toString() {
        return "Familiarity: "+ getFamiliarity();
    }

    @Override
    public HashMap<String, Object> toHashMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("familiarity",this.familiarity);
        return map;
    }
}
