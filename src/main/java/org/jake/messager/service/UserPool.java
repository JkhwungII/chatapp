package org.jake.messager.service;

import jakarta.annotation.Resource;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.jake.messager.MessagerApplication;
import org.jake.messager.chatuser.ChatUser;
import org.jake.messager.chatuser.ChatUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserPool {
    @Autowired
    ChatUserRepository chatUserRepository;
    @Autowired
    StringRedisTemplate stringRedisTemplate;


    private static final Logger logger = LoggerFactory.getLogger(MessagerApplication.class);
    @Transactional
    public void addUserToPending(String UUID){
        chatUserRepository.setIsPairing(UUID);

    }

    @Transactional
    public String pollUser(String UUID){
        int polled_index = -1;
        String chosen_user_uuid;
        List<ChatUser> pending_queue = chatUserRepository.findByPairingTrue();

        if (chatUserRepository.findByID(UUID).getPairedWith() != null) {
            return getUserPair(UUID);
        }

        if (pending_queue.size() >= 2){
            polled_index = (int)(Math.random() * pending_queue.size());
            chosen_user_uuid = pending_queue.get(polled_index).getID();
            while (chosen_user_uuid.equals(UUID)){
                polled_index = (int)(Math.random() * pending_queue.size());
                chosen_user_uuid = pending_queue.get(polled_index).getID();
            }
        }
        else
            return "waiting";

        chatUserRepository.setPair(UUID,chosen_user_uuid,true);
        chatUserRepository.setPair(chosen_user_uuid,UUID,false);

        return getUserPair(UUID);
    }

    public String getUserPair(String UUID){

        boolean hasValue = stringRedisTemplate.opsForHash().hasKey(UUID,"user_pair");
        if (hasValue)
            return (String) stringRedisTemplate.opsForHash().get(UUID,"user_pair");

        ChatUser c = chatUserRepository.findByID(UUID);
        String user_pair;

        assert c != null;
        if (c.getPairedWith() == null){
            return null;
        }

        if (c.isFirst()){
            user_pair = c.getID() + ";" + c.getPairedWith();
        } else {
            user_pair = c.getPairedWith() + ";" + c.getID();
        }

        stringRedisTemplate.opsForHash().put(UUID,"user_pair", user_pair);
        return user_pair;

    }

    public String getPairedUserSessionID(String UUID){
        boolean hasValue = stringRedisTemplate.opsForHash().hasKey(UUID,"paired_user_session_ID");
        if (hasValue)
            return (String) stringRedisTemplate.opsForHash().get(UUID,"paired_user_session_ID");

        String paired_user_session_ID = chatUserRepository.findPairedSessionID(UUID);
        stringRedisTemplate.opsForHash().put(UUID,"paired_user_session_ID", paired_user_session_ID);

        return paired_user_session_ID;
    }

    @Transactional
    public  void deleteUserPair(String UUID){
        ChatUser c = chatUserRepository.findByID(UUID);
        if (c != null){
            String toRemove =  c.getPairedWith();
            stringRedisTemplate.delete(UUID);
            stringRedisTemplate.delete(toRemove);
            chatUserRepository.removePair(c.getID());
            chatUserRepository.removePair(toRemove);
        } else {
            throw new IllegalArgumentException("ID \""+UUID+"\" cannot be found in DB");
        }
        logger.info("pair deleted");
    }


    @Transactional
    public void registerUser(String UUID, String SessionID){
        if (chatUserRepository.findByID(UUID)==null) {
            ChatUser u = new ChatUser();
            u.setID(UUID);
            u.setSessionID(SessionID);
            chatUserRepository.save(u);
        } else {
            chatUserRepository.setSessionID(SessionID,UUID);
        }
    }
    @Transactional
    public synchronized void leavePendingQueue(String UUID){
        chatUserRepository.unsetIsPairing(UUID);
        logger.info("pending queue fetched from DB" + chatUserRepository.findByPairingTrue());
    }
}
