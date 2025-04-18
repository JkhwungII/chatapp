package org.jake.messager.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClientSideMessage {
    String date;
    String message;
    Boolean is_from_you;
}
