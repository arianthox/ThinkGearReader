package com.globant.brainwaves.model;

import java.util.HashMap;
import java.util.logging.Level;

public interface Packet {
    default Level getLogLevel(){
        return Level.INFO;
    }
    HashMap<String,Object> toHashMap();
}
