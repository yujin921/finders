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
    public ChatRoomDTO getOrCreateChatRoomByProjectNum(int projectNum) {
        Optional<ChatRoomEntity> existingChatRoom = chatRoomRepository.findByProjectNum(projectNum);

        if (existingChatRoom.isEmpty()) {
            // 채팅방이 없으면 자동으로 새 채팅방을 생성
            ChatRoomDTO createdChatRoom = createChatRoom(projectNum, "프로젝트 " + projectNum + "의 채팅방");

            // 채팅방이 생성된 후 참가자 자동 추가
            addParticipantsToChatRoom(createdChatRoom.getChatroomId(), projectNum);

            return createdChatRoom;
        } else {
            // 존재하면 DTO로 변환하여 반환
            return convertEntityToDTO(existingChatRoom.get());
        }
    }

    // 새로운 채팅방 생성
    private ChatRoomDTO createChatRoom(int projectNum, String chatroomName) {
        ChatRoomEntity chatRoom = ChatRoomEntity.builder()
                .projectNum(projectNum)
                .chatroomName(chatroomName)
                .createdTime(new Timestamp(System.currentTimeMillis()))
                .build();

        ChatRoomEntity savedChatRoom = chatRoomRepository.save(chatRoom);
        return convertEntityToDTO(savedChatRoom);
    }

    // 프로젝트 참가자를 채팅방 참가자로 추가하는 메서드
    @Transactional
    private void addParticipantsToChatRoom(int chatroomId, int projectNum) {
        // 프로젝트에 참여한 모든 멤버를 조회
        List<MemberEntity> projectMembers = memberRepository.findByProjects_ProjectNum(projectNum);

        // 프로젝트 멤버가 없을 때 처리
        if (projectMembers.isEmpty()) {
            System.out.println("참가자가 없습니다. 프로젝트 번호: " + projectNum);
            return;
        }

        for (MemberEntity member : projectMembers) {
            try {
                ChatParticipantEntity participant = ChatParticipantEntity.builder()
                        .chatroomId(chatroomId)
                        .participantId(member.getMemberId()) // 올바른 필드로 설정
                        .joinedTime(new Timestamp(System.currentTimeMillis()))
                        .build();
                chatParticipantRepository.save(participant);
                System.out.println("참가자 추가됨: " + member.getMemberId());
            } catch (Exception e) {
                // 예외 발생 시 로그 출력
                System.err.println("참가자 추가 중 오류 발생: " + e.getMessage());
                e.printStackTrace();
            }
        }
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
