package org.jake.messager.chatuser;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Entity
public class ChatUser implements Serializable {
    @Id
    @Column(name = "ID", nullable = false)
    String ID;

    @Column(name = "pairedWith")
    String pairedWith;

    @Column(name = "sessionID")
    String sessionID;

    @Column(name = "pairing")
    boolean pairing;

    @Column(name = "first")
    boolean first;

    @Override
    public String toString() {
        return "ChatUser{" +
                "ID='" + ID + '\'' +
                '}';
    }
}
