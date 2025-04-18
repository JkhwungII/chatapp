package org.jake.messager.message;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Message implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "UserPair", nullable = false)
    private String userPair;

    @Column(name = "TimeSent", nullable = false)
    private LocalDateTime timeSent;

    @Column(name = "FromUser", nullable = false)
    private String fromUser;

    @Column(name = "Message", nullable = false)
    private String message;
}
