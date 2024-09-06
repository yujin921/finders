package net.datasa.finders.domain.dto;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatParticipantDTO {
    private int chatroomId;
    private String participantId;
    private Timestamp joinedTime;
}