package com.globant.brainwaves.client;

import com.globant.brainwaves.model.BufferRawPacket;
import com.globant.brainwaves.model.ChannelPacket;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.util.List;

@FeignClient(name = "CoreEngine")
@RequestMapping(value = "/api/core-engine")
public interface CoreEngineClient {

    @PostMapping("/packet/raw/receive/{deviceId}/{sessionId}")
    ResponseEntity receive(@PathVariable("deviceId") String id,@PathVariable("sessionId") String sessionId,@RequestBody @Valid BufferRawPacket... packet);

    @PostMapping("/packet/channel/receive/{deviceId}/{sessionId}")
    ResponseEntity receive(@PathVariable("deviceId") String id,@PathVariable("sessionId") String sessionId,@RequestBody @Valid ChannelPacket... packet);

}
