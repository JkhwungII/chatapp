package org.jake.messager.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.jake.messager.MessagerApplication;
import org.jake.messager.service.UserPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.UUID;

@Component
public class ChatSessionHandshakeInterceptor implements HandshakeInterceptor {

    public UserPool userPool;

    private static final Logger logger = LoggerFactory.getLogger(MessagerApplication.class);

    public ChatSessionHandshakeInterceptor(UserPool userPool){ this.userPool = userPool; }
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        boolean has_user_ID = false;
        boolean is_user_paired;

        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        Cookie[] cookies = servletRequest.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                String value = cookie.getValue();
                if (name.equals("user_ID")){
                    logger.info("old user: "+ value);
                    has_user_ID = true;
                    attributes.put("user_ID", value);
                }
            }
        }

        if (!has_user_ID){
            String user_ID = UUID.randomUUID().toString();
            logger.info("set new user ID: "+user_ID);
            attributes.put("user_ID", user_ID);
            response.getHeaders().add("Set-Cookie", "user_ID=" + user_ID + "; HttpOnly;  Max-Age=315360000");
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
    }
}
