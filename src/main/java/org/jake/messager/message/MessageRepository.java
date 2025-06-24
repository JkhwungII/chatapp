package org.jake.messager.message;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByUserPairOrderByTimeSent(String user_Pair);

    @Transactional
    void deleteByUserPair(String user_Pair);
}
