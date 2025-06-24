package org.jake.messager.service;

import org.jake.messager.message.ClientSideMessage;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;

@Service
public class RedisPubSubService {
    private StringRedisTemplate redisTemplate;
    private String redisChannel;
    private List<WebSocketSession> sessions_pool;


    public RedisPubSubService(StringRedisTemplate redisTemplate, String redisChannel){
        this.redisTemplate = redisTemplate;
        this.redisChannel = redisChannel;
    }

    public void setSessions_pool(List<WebSocketSession> sessions) {
        this.sessions_pool = sessions;
    }

    public void relayMessage(String receiver_ID, String message){
        redisTemplate.convertAndSend(redisChannel, receiver_ID+"::"+message);
    }

    public void processRelayedMessage(String message) throws IOException {
        String[] decoded =  message.split(":");
        for (WebSocketSession s: sessions_pool) {
            if(decoded[0].equals(s.getId())){
                s.sendMessage(new TextMessage(decoded[1]));
                break;
            }
        }
    }
}
