package net.datasa.finders.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.datasa.finders.domain.entity.ChatParticipantEntity;
import net.datasa.finders.domain.entity.ChatRoomEntity;
import net.datasa.finders.domain.entity.TeamEntity;
import net.datasa.finders.repository.ChatParticipantRepository;
import net.datasa.finders.repository.ChatRoomRepository;
import net.datasa.finders.repository.TeamRepository;

@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantEntityRepository;
    private final TeamRepository teamRepository;

    @Autowired
    public ChatRoomService(ChatRoomRepository chatRoomRepository, 
                           ChatParticipantRepository chatParticipantEntityRepository,
                           TeamRepository teamRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatParticipantEntityRepository = chatParticipantEntityRepository;
        this.teamRepository = teamRepository;
    }

    // 모든 채팅방 조회 메서드 추가
    public List<ChatRoomEntity> getAllChatRooms() {
        return chatRoomRepository.findAll();
    }

    // member_id가 속한 모든 project_num에 대해 채팅방 생성
    @Transactional
    public void createChatRoomsForAllMemberProjects(String memberId) {
        // TeamEntity에서 해당 member_id가 속한 모든 project_num 조회
        List<Integer> projectNums = teamRepository.findByMemberId(memberId).stream()
                .map(TeamEntity::getProjectNum)
                .distinct()
                .collect(Collectors.toList());

        for (Integer projectNum : projectNums) {
            // 채팅방이 이미 존재하는지 확인
            if (!chatRoomRepository.existsByProjectNum(projectNum)) {
                // 채팅방 생성
                ChatRoomEntity chatRoom = ChatRoomEntity.builder()
                        .projectNum(projectNum)
                        .chatroomName("프로젝트 " + projectNum + " 채팅방")
                        .createdTime(LocalDateTime.now())
                        .build();
                ChatRoomEntity savedChatRoom = chatRoomRepository.save(chatRoom);

                // 팀의 멤버들을 채팅방 참여자로 추가
                List<TeamEntity> teamMembers = teamRepository.findByProjectNum(projectNum);
                for (TeamEntity member : teamMembers) {
                    ChatParticipantEntity participant = ChatParticipantEntity.builder()
                            .chatroomId(savedChatRoom.getChatroomId())
                            .participantId(member.getMemberId())
                            .joinedTime(LocalDateTime.now())
                            .build();
                    chatParticipantEntityRepository.save(participant);
                }
            }
        }
    }
}
