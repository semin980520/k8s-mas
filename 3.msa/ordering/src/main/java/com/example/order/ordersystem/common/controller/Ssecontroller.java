package com.example.order.ordersystem.common.controller;

import com.example.order.ordersystem.common.repository.SseEmitterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequestMapping("/sse")
public class Ssecontroller {

    private final SseEmitterRegistry sseEmitterRegistry;

    @Autowired
    public Ssecontroller(SseEmitterRegistry sseEmitterRegistry) {
        this.sseEmitterRegistry = sseEmitterRegistry;
    }

    @GetMapping("/connect")
    public SseEmitter connect(@RequestHeader("X-User-Email")String email) throws IOException {
        System.out.println("connect start");
        SseEmitter sseEmitter = new SseEmitter(60 * 60 * 1000L); // 한 시간 유효 시간
        sseEmitterRegistry.addSeeEmitter(email, sseEmitter);
        sseEmitter.send(SseEmitter.event().name("connect").data("연결완료"));
        System.out.println("연결완료 : "+ sseEmitter);
        return sseEmitter;
    }
    @GetMapping("/disconnect")
    public void disConnect(@RequestHeader("X-User-Email")String email) throws IOException {
        System.out.println("disconnect start");
        sseEmitterRegistry.removeEmitter(email);

    }
}
