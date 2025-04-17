package org.jake.messager.service;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.jake.messager.MessagerApplication;
import org.jake.messager.chatuser.ChatUser;
import org.jake.messager.chatuser.ChatUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserPool {
    BidiMap<String,String> paired_users = new DualHashBidiMap<>();
    List<String> pending_users = Collections.synchronizedList(new ArrayList<>());
    Map<String,String> user_to_SessionID = Collections.synchronizedMap(new HashMap<>());
    @Autowired
    ChatUserRepository chatUserRepository;

    private static final Logger logger = LoggerFactory.getLogger(MessagerApplication.class);

    public synchronized void addUserToPending(String UUID){
        if(!pending_users.contains(UUID)){
            pending_users.add(UUID);
        }
        chatUserRepository.setIsPairing(UUID);

    }

    @Transactional
    public synchronized String pollUser(String UUID){
        int polled_index = -1;
        String chosen_user_uuid;
        logger.info("local pending queue:" + pending_users.toString());
        logger.info("pending queue fetched from DB" + chatUserRepository.findByPairingTrue());

        if (paired_users.getKey(UUID) != null) {
            logger.info("DB fetch first user:" + chatUserRepository.findByID(UUID).getPairedWith());
            return getUserPair(UUID);
        }

        if (pending_users.size() >= 2){
            polled_index = (int)(Math.random() * pending_users.size());
            chosen_user_uuid = pending_users.get(polled_index);
            while (chosen_user_uuid.equals(UUID)){
                polled_index = (int)(Math.random() * pending_users.size());
                chosen_user_uuid = pending_users.get(polled_index);
            }
        }
        else
            return "waiting";

        chatUserRepository.setPair(UUID,chosen_user_uuid,true);
        chatUserRepository.setPair(chosen_user_uuid,UUID,false);
        paired_users.put(UUID, chosen_user_uuid);
        return getUserPair(UUID);
    }

    public synchronized String getUserPair(String UUID){
        String primary = paired_users.getKey(UUID);
        String secondary = paired_users.get(UUID);
        ChatUser c = chatUserRepository.findByID(UUID);
        if (c.isFirst() && c.getPairedWith() != null){
            logger.info("DB fetched user pair: " + c.getID() + ";" + c.getPairedWith());
        } else if (c.getPairedWith() != null) {
            logger.info("DB fetched user pair: " + c.getPairedWith() + ";" + c.getID());
        }

        if (primary == null && secondary != null) {
            return UUID + ";" + secondary;
        } else if (secondary == null && primary != null){
            return primary + ";" + UUID;
        } else
            return null;
    }

    public synchronized String getPairedUserSessionID(String UUID){
        String primary = paired_users.getKey(UUID);
        String secondary = paired_users.get(UUID);

        logger.info("fetch paired user session ID:  "+ chatUserRepository.findPairedSessionID(UUID));
        if (primary != null) {
            return user_to_SessionID.get(primary);
        } else if (secondary != null) {
            return user_to_SessionID.get(secondary);
        } else
            return null;
    }

    @Transactional
    public synchronized void deleteUserPair(String UUID){
        if(paired_users.containsKey(UUID) || paired_users.containsValue(UUID))
            paired_users.removeValue(UUID);
        ChatUser c = chatUserRepository.findByID(UUID);
        if (c != null){
            chatUserRepository.removePair(c.getID());
            chatUserRepository.removePair(c.getPairedWith());
        }
        logger.info("pair deleted");
    }

    public synchronized String getUserSessionID(String UUID){
        return user_to_SessionID.get(UUID);
    }

    public synchronized void registerUser(String UUID, String SessionID){
        user_to_SessionID.put(UUID,SessionID);
        if (chatUserRepository.findByID(UUID)==null) {
            ChatUser u = new ChatUser();
            u.setID(UUID);
            u.setSessionID(SessionID);
            chatUserRepository.save(u);
        } else {
            chatUserRepository.setSessionID(SessionID,UUID);
        }
        logger.info("session_id added "+user_to_SessionID.get(UUID));
    }

    public synchronized void leavePendingQueue(String UUID){
        pending_users.remove(UUID);
        chatUserRepository.unsetIsPairing(UUID);
        logger.info("local pending queue:" + pending_users.toString());
        logger.info("pending queue fetched from DB" + chatUserRepository.findByPairingTrue());
        logger.info(pending_users.toString());
    }
}
