package com.example.order.ordersystem.common.repository;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterRegistry {
//    Sseemitter객체는 사용자의 연결정보(ip, macaddress 등) 을 의미
//    ConcurrentHashMap thread-safe한 map(동시성 이슈 발생x)
    private Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    public void addSeeEmitter(String email,SseEmitter sseEmitter){
        this.emitterMap.put(email,sseEmitter);
    }

    public SseEmitter getEmitter(String email){
        return this.emitterMap.get(email);
    }
    public void removeEmitter(String email){
        this.emitterMap.remove(email);
        System.out.println(emitterMap.size());
    }
}

