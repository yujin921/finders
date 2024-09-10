package net.datasa.finders.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateChatRoomRequestDTO {
    private int projectNum; // 선택된 프로젝트 번호
    private List<String> selectedMemberIds = new ArrayList<>(); // 선택된 멤버 ID 목록, 빈 리스트로 초기화
    private String chatRoomName; // 채팅방 이름 필드 추가
    // 필요한 경우 다른 생성자도 제공
    public CreateChatRoomRequestDTO(int projectNum) {
        this.projectNum = projectNum;
        this.selectedMemberIds = new ArrayList<>(); // 빈 리스트로 초기화
        this.chatRoomName = chatRoomName;
    }
    
}
