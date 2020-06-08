package com.globant.brainwaves.client;

import com.globant.brainwaves.model.BufferRawPacket;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.util.List;

@FeignClient(name = "CoreEngine")
@RequestMapping(value = "/api/core-engine/packet/raw")
public interface BufferRawPacketClient {

    @PostMapping("/receive/{deviceId}")
    ResponseEntity receive(@PathVariable("deviceId") String id,@RequestBody @Valid BufferRawPacket... packet);

}
