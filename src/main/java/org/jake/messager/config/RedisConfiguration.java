package org.jake.messager.config;

import org.jake.messager.service.RedisPubSubService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisConfiguration {

    @Bean
    public RedisMessageListenerContainer container (RedisConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter){
        RedisMessageListenerContainer listenerContainer = new RedisMessageListenerContainer();
        listenerContainer.setConnectionFactory(connectionFactory);
        listenerContainer.addMessageListener(listenerAdapter, new PatternTopic(redisChannel()));

        return listenerContainer;
    }

    @Bean
    public MessageListenerAdapter listenerAdapter(RedisPubSubService service){
        return new MessageListenerAdapter(service,"processRelayedMessage");
    }

    @Bean
    StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }
    @Bean
    public String redisChannel(){
        return "ReceiverRelay";
    }
}
