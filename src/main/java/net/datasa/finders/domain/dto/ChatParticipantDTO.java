package net.datasa.finders.domain.dto;

import java.sql.Timestamp;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class ChatParticipantDTO {
    private int chatroomId;
    private String participantId;
    private Timestamp joinedTime;
}