package net.datasa.finders.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


//채팅전용
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InviteRequestDTO {
    private int chatroomId;
    private String memberId;
}
