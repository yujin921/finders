package net.datasa.finders.domain.dto;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//채팅전용
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDTO {
    private int chatroomId;
    private int projectNum; // 프로젝트 번호 추가
    private String chatroomName;
    private Timestamp createdTime;
}

