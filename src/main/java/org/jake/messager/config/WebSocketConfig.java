package org.jake.messager.config;

import org.jake.messager.interceptor.ChatSessionHandshakeInterceptor;
import org.jake.messager.message.MessageRepository;
import org.jake.messager.service.UserPool;
import org.jake.messager.handler.ChatSessionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import java.util.Optional;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    UserPool userPool;
    ChatSessionHandler chatSessionHandler;
    ChatSessionHandshakeInterceptor chatSessionHandshakeInterceptor;

    public WebSocketConfig(UserPool userPool,
                           ChatSessionHandler chatSessionHandler,
                           ChatSessionHandshakeInterceptor chatSessionHandshakeInterceptor)
    {
        this.userPool = userPool;
        this.chatSessionHandler = chatSessionHandler;
        this.chatSessionHandshakeInterceptor = chatSessionHandshakeInterceptor;
    }


    String Origin = Optional.ofNullable(System.getenv("ORIGIN")).orElse("*");



    @Override
    public void registerWebSocketHandlers( WebSocketHandlerRegistry registry){
        registry.addHandler(chatSessionHandler,"/socket")
                .addInterceptors(new ChatSessionHandshakeInterceptor(userPool))
                .setAllowedOrigins(Origin);
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(8192*1024);
        return container;
    }

}
