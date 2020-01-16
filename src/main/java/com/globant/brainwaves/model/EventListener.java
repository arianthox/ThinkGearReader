package com.globant.brainwaves.model;

public interface EventListener {
    void processPacket(Packet in);
}