package net.datasa.finders.domain.entity;

import lombok.*;

import java.io.Serializable;

//채팅전용
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatParticipantId implements Serializable {
    private int chatroomId;
    private String participantId;
}

