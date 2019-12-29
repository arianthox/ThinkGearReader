package com.globant.brainwaves.model;

import java.util.HashMap;

/**
 * Created by root on 19.05.2017.
 */
public class MentalEffortPacket implements Packet{
    private int mentalEffort;

    public int getMentalEffort() {
        return mentalEffort;
    }

    @Override
    public String toString() {
        return "MentalEffort: "+ getMentalEffort();
    }

    @Override
    public HashMap<String, Object> toHashMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("mentalEffort",this.mentalEffort);
        return map;
    }
}
