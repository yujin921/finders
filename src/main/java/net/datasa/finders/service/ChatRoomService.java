package net.datasa.finders.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.datasa.finders.domain.dto.ChatRoomDTO;
import net.datasa.finders.domain.entity.ChatParticipantEntity;
import net.datasa.finders.domain.entity.ChatRoomEntity;
import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.repository.ChatParticipantRepository;
import net.datasa.finders.repository.ChatRoomRepository;
import net.datasa.finders.repository.MemberRepository;

@Service
public class ChatRoomService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatParticipantRepository chatParticipantRepository;

    @Autowired
    private MemberRepository memberRepository;

    // 프로젝트 번호로 채팅방 조회 및 자동 생성
    @Transactional
    public ChatRoomDTO getOrCreateChatRoomByProjectNum(int projectNum, String userId) {
        System.out.println("Service received projectNum: " + projectNum);
        System.out.println("User ID: " + userId);

        // 사용자가 해당 프로젝트에 속해 있는지 검증
        if (!isUserAssignedToProject(userId, projectNum)) {
            System.out.println("User " + userId + " is not assigned to project " + projectNum);
            throw new IllegalArgumentException("User is not assigned to the project.");
        }

        Optional<ChatRoomEntity> existingChatRoom = chatRoomRepository.findByProjectNum(projectNum);
        if (existingChatRoom.isEmpty()) {
            System.out.println("채팅방이 존재하지 않으므로 새로 생성합니다.");

            // 채팅방 생성
            ChatRoomDTO createdChatRoom = createChatRoom(projectNum, "프로젝트 " + projectNum + "의 채팅방");

            // 생성 후 참가자 자동 추가
            addParticipantsToChatRoom(createdChatRoom.getChatroomId(), projectNum);

            return createdChatRoom;
        } else {
            System.out.println("기존 채팅방을 찾았습니다: " + existingChatRoom.get().getChatroomName());
            // 존재하는 채팅방을 DTO로 변환하여 반환
            return convertEntityToDTO(existingChatRoom.get());
        }
    }

    // 새로운 채팅방 생성 메서드
    private ChatRoomDTO createChatRoom(int projectNum, String chatroomName) {
        System.out.println("Creating new chat room for projectNum: " + projectNum);
        ChatRoomEntity chatRoom = ChatRoomEntity.builder()
                .projectNum(projectNum)
                .chatroomName(chatroomName)
                .createdTime(new Timestamp(System.currentTimeMillis()))
                .build();

        ChatRoomEntity savedChatRoom = chatRoomRepository.save(chatRoom);
        System.out.println("새로운 채팅방이 생성되었습니다: " + savedChatRoom.getChatroomName());
        return convertEntityToDTO(savedChatRoom);
    }

    // 프로젝트 참가자를 채팅방 참가자로 추가하는 메서드
    @Transactional
    private void addParticipantsToChatRoom(int chatroomId, int projectNum) {
        List<MemberEntity> projectMembers = memberRepository.findByProjects_ProjectNum(projectNum);

        if (projectMembers.isEmpty()) {
            System.out.println("프로젝트에 참가자가 없습니다. 프로젝트 번호: " + projectNum);
            return;
        }

        for (MemberEntity member : projectMembers) {
            try {
                ChatParticipantEntity participant = ChatParticipantEntity.builder()
                        .chatroomId(chatroomId)
                        .participantId(member.getMemberId())
                        .joinedTime(new Timestamp(System.currentTimeMillis()))
                        .build();
                chatParticipantRepository.save(participant);
                System.out.println("참가자 추가됨: " + member.getMemberId());
            } catch (Exception e) {
                System.err.println("참가자 추가 중 오류 발생: " + e.getMessage());
            }
        }
    }

    // 사용자가 프로젝트에 속해 있는지 확인하는 메서드
    private boolean isUserAssignedToProject(String userId, int projectNum) {
        List<MemberEntity> members = memberRepository.findByMemberIdAndProjects_ProjectNum(userId, projectNum);
        System.out.println("프로젝트 멤버 조회 결과: " + members);
        return !members.isEmpty();
    }

    // Entity를 DTO로 변환하는 메서드
    private ChatRoomDTO convertEntityToDTO(ChatRoomEntity chatRoom) {
        return ChatRoomDTO.builder()
                .chatroomId(chatRoom.getChatroomId())
                .chatroomName(chatRoom.getChatroomName())
                .createdTime(chatRoom.getCreatedTime())
                .build();
    }
}
