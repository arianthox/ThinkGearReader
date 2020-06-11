package com.globant.brainwaves.model;

import com.globant.brainwaves.commons.model.Packet;

public interface EventListener {
    void processPacket(Packet in);
}