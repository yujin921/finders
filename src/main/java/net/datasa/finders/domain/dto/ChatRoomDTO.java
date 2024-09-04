package net.datasa.finders.domain.dto;

import java.sql.Timestamp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatRoomDTO {
    private int chatroomId;
    private String chatroomName;
    private Timestamp createdTime;
}
