package org.jake.messager.chatuser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatUserRepository extends JpaRepository<ChatUser, String> {

    ChatUser findByID(String ID);
    List<ChatUser> findByPairingTrue();
    @Query("SELECT u.sessionID FROM ChatUser u WHERE u.ID = (SELECT u1.ID FROM ChatUser u1 WHERE u1.pairedWith = ?1)")
    String findPairedSessionID(String ID);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ChatUser u SET u.pairing = true WHERE u.ID = ?1")
    void setIsPairing(String ID);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ChatUser u SET u.pairedWith = ?2, u.first = ?3  WHERE u.ID = ?1")
    void setPair(String to_be_paired, String paired, boolean is_first);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ChatUser u SET u.sessionID = ?1 WHERE u.ID = ?2")
    void setSessionID(String session_ID, String ID);
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ChatUser u SET u.pairing = false WHERE u.ID = ?1")
    void unsetIsPairing(String ID);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ChatUser u SET u.pairedWith = null WHERE u.ID = ?1")
    void removePair(String ID);

    @Modifying(clearAutomatically = true)
    void deleteByID(String ID);


}
