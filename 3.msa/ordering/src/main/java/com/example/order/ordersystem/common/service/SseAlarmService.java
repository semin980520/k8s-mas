package com.example.order.ordersystem.common.service;

import com.example.order.ordersystem.common.dto.SseMessageDto;
import com.example.order.ordersystem.common.repository.SseEmitterRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Component
public class SseAlarmService implements MessageListener {
    private final SseEmitterRegistry sseEmitterRegistry;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String , String > redisTemplate;
    @Autowired
    public SseAlarmService(SseEmitterRegistry sseEmitterRegistry, ObjectMapper objectMapper,@Qualifier("ssePubSub") RedisTemplate<String, String> redisTemplate) {
        this.sseEmitterRegistry = sseEmitterRegistry;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    public void sendMessage(String receiver, String sender, String message) {

        SseMessageDto dto = SseMessageDto.builder()
                .receiver(receiver)
                .sender(sender)
                .message(message)
                .build();
        System.out.println(dto);

        try {
            SseEmitter sseEmitter = sseEmitterRegistry.getEmitter(receiver);
            String data = objectMapper.writeValueAsString(dto);
//            만약에 emitter객체가 현재 서버에 있으면, 바로 알림 발송. 그렇지 않으면, redis pub/sub활용
            if (sseEmitter != null){
                sseEmitter.send(SseEmitter.event().name("ordered").data(data));
//                사용자가 새로고침후에 알림메세지를 조회하려면 DB에 추가적으로 저장 필요.
            }else {
//            redis pub sub기능을 활용하여 메세지 publish
                redisTemplate.convertAndSend("order-channel", data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
//        message : 실직적으로 메세지가 담겨 있는 객체
//        patter : 채널명
//        추후 여러개의 채널에 각기 메세지를 publish하고 subscribe 할 경우 채널명으로 분기처리 가능
        String channelName = new String(pattern);
//        if (channelName.equals("order-channel")){ // 분기처리 하는 방법 채널 명으로 분기 처리 필요 시
//        }
        System.out.println("channelName : "+ channelName);

        try {
            SseMessageDto dto = objectMapper.readValue(message.getBody(), SseMessageDto.class);
            SseEmitter sseEmitter = sseEmitterRegistry.getEmitter(dto.getReceiver());
//            해당 서버에 receiver의 emitter객체가 있으면 send
            if (sseEmitter != null){
                String data = objectMapper.writeValueAsString(sseEmitter);
            sseEmitter.send(SseEmitter.event().name("ordered").data(data));

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}