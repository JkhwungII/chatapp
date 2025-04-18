package org.jake.messager.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jake.messager.MessagerApplication;
import org.jake.messager.message.ClientSideMessage;
import org.jake.messager.message.Message;
import org.jake.messager.message.MessageRepository;
import org.jake.messager.service.UserPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;


public  class ChatSessionHandler extends TextWebSocketHandler {

    UserPool userPool;
    MessageRepository messageRepository;

    private static final Logger logger = LoggerFactory.getLogger(MessagerApplication.class);

    ObjectMapper objectMapper = new ObjectMapper();
    public ChatSessionHandler(UserPool userPool, MessageRepository messageRepository){
        this.userPool = userPool;
        this.messageRepository = messageRepository;
    }
    List<WebSocketSession> sessions_pool = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String user_ID = (String) session.getAttributes().get("user_ID");
        userPool.registerUser(user_ID, session.getId());
        sessions_pool.add(session);
        session.sendMessage(new TextMessage("#CONNECTED: " + user_ID));
    }
    @Override
    protected synchronized void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception  {
        String user_ID = (String) session.getAttributes().get("user_ID");
        logger.info("incoming message: "+ message.getPayload());
        if (message.getPayload().equals("#PAIR")) {
            userPool.addUserToPending(user_ID);
            pairUser(session,user_ID);
        } else if (message.getPayload().equals("#ASK_IF_PAIRED")){
            if (userPool.getUserPair(user_ID) != null) {
                session.sendMessage(new TextMessage("#IS_PAIRED"));
            }else {
                session.sendMessage(new TextMessage("#NOT_PAIRED"));
            }
            logger.info("user pair: " + userPool.getUserPair(user_ID));
        } else if (message.getPayload().equals("#LEAVE")) {
            logger.info("User :"+user_ID+" send leave");
            String paired_session_ID = userPool.getPairedUserSessionID(user_ID);
            messageRepository.deleteByUserPair(userPool.getUserPair(user_ID));
            userPool.deleteUserPair(user_ID);
            for (WebSocketSession wss: sessions_pool){
                if (wss.getId().equals(paired_session_ID)){
                    logger.info("User :"+paired_session_ID+" alive and leaving");
                    wss.sendMessage(new TextMessage("#LEAVE_COMPLETE"));
                    break;
                }
            }

            session.sendMessage(new TextMessage("#LEAVE_COMPLETE"));
        }  else if (message.getPayload().contains("#FETCH_PREVIOUS_MESSAGES")) {
            String user_pair = userPool.getUserPair(user_ID);
            List<Message> previous_messages = messageRepository.findByUserPairOrderByTimeSent(user_pair);
            previous_messages.forEach( m ->{
                String content = null;
                try {
                    content = objectMapper.writeValueAsString(new ClientSideMessage(m.getTimeSent().toString(), m.getMessage(), user_ID.equals(m.getFromUser())));
                    session.sendMessage(new TextMessage("@PREVIOUS_MESSAGES::"+content));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            session.sendMessage(new TextMessage("@END_OF_MESSAGES"));
        }  else {
            LocalDateTime now_time = LocalDateTime.now();
            session.sendMessage(
                    new TextMessage(
                            objectMapper.writeValueAsString(
                                    new ClientSideMessage(now_time.toString(), message.getPayload(), true)
                            )
                    )
            );

            String destination_session_ID = userPool.getPairedUserSessionID(user_ID);
            for (WebSocketSession s: sessions_pool) {
                if(destination_session_ID.equals(s.getId())){
                    s.sendMessage(
                            new TextMessage(
                                    objectMapper.writeValueAsString(
                                            new ClientSideMessage(now_time.toString(), message.getPayload(), false)
                                    )
                            )
                    );
                    break;
                }
            }
            Message m = new Message();
            m.setUserPair(userPool.getUserPair(user_ID));
            m.setFromUser(user_ID);
            m.setTimeSent(now_time);
            m.setMessage(message.getPayload());
            messageRepository.save(m);
        }
    }
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception{
        if (session.isOpen()){
            try {
                session.close();
            } catch (Exception ignored){

            }
        }
        sessions_pool.remove(session);
    }

      Void pairUser(WebSocketSession session, String user_ID) throws Exception {
        CompletableFuture<String> paired_user = CompletableFuture.supplyAsync(() -> {
            String pollResult = "waiting";
            try {
                int pair_counter = 0;
                while (pair_counter < 15) {
                    pollResult = userPool.pollUser(user_ID);
                    if(pollResult.equals("waiting")) {
                        Thread.sleep(500);
                        pair_counter += 1;
                    }
                    else
                        break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return pollResult;
        });
        paired_user.thenAccept((result)->{
            try {
                if ("waiting".equals(result) && userPool.getUserPair(user_ID) == null) {
                    session.sendMessage(new TextMessage("#QUEUE_EMPTY"));
                    userPool.leavePendingQueue(user_ID);
                }
                else if (result != null){
                    session.sendMessage(new TextMessage("#PAIRED"));
                    userPool.leavePendingQueue(user_ID);
                } else {
                    logger.error("null result when pairing user: "+ user_ID);
                    userPool.leavePendingQueue(user_ID);
                }
                logger.info("paired result :" + result);
            }catch (Exception e){
                logger.error(e.toString());
            }
        });

        return null;
    }
}
