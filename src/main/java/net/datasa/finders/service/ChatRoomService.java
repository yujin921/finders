package net.datasa.finders.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.datasa.finders.domain.dto.ChatMessageDTO;
import net.datasa.finders.domain.entity.ChatMessageEntity;
import net.datasa.finders.domain.entity.ChatParticipantEntity;
import net.datasa.finders.domain.entity.ChatRoomEntity;
import net.datasa.finders.domain.entity.TeamEntity;
import net.datasa.finders.repository.ChatMessageRepository;
import net.datasa.finders.repository.ChatParticipantRepository;
import net.datasa.finders.repository.ChatRoomRepository;
import net.datasa.finders.repository.TeamRepository;

@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final TeamRepository teamRepository;
    private final ChatMessageService chatMessageService;

    @Autowired
    public ChatRoomService(ChatRoomRepository chatRoomRepository, 
                           ChatParticipantRepository chatParticipantRepository,
                           TeamRepository teamRepository,
                           ChatMessageService chatMessageService) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.teamRepository = teamRepository;
        this.chatMessageService = chatMessageService;
    }

    // 모든 채팅방 조회 메서드
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
                    chatParticipantRepository.save(participant);
                }
            }
        }
    }

    // 메시지 저장 메서드 (ChatMessageService로 위임)
    @Transactional
    public void saveMessage(ChatMessageDTO chatMessageDTO) {
        chatMessageService.saveMessage(chatMessageDTO);
    }

    // 특정 채팅방의 모든 메시지 조회 (ChatMessageService로 위임)
    public List<ChatMessageDTO> getMessagesForChatRoom(int chatroomId) {
        return chatMessageService.getAllMessagesForChatroom(chatroomId);
    }

    // 채팅방 ID로 채팅방 정보 조회
    public ChatRoomEntity getChatRoomById(int chatroomId) {
        return chatRoomRepository.findById(chatroomId).orElse(null);
    }
}

