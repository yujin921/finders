package net.datasa.finders.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.datasa.finders.domain.dto.ChatMessageDTO;
import net.datasa.finders.domain.dto.ChatRoomDTO;
import net.datasa.finders.domain.dto.CreateChatRoomRequestDTO;
import net.datasa.finders.domain.dto.ProjectDTO;
import net.datasa.finders.domain.entity.ChatParticipantEntity;
import net.datasa.finders.domain.entity.ChatRoomEntity;
import net.datasa.finders.domain.entity.TeamEntity;
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

    // 현재 사용자가 속한 채팅방만 조회
    public List<ChatRoomEntity> getChatRoomsForMember(String memberId) {
        List<Integer> chatroomIds = chatParticipantRepository.findByParticipantId(memberId)
                .stream()
                .map(ChatParticipantEntity::getChatroomId)
                .distinct()
                .collect(Collectors.toList());
        
        return chatRoomRepository.findAllById(chatroomIds);
    }

    // 새로운 채팅방 생성 메서드
    @Transactional
    public void createChatRoom(CreateChatRoomRequestDTO request, String loggedInUserId) {
        // 채팅방 이름 설정
        String chatRoomName = request.getChatRoomName() != null && !request.getChatRoomName().isEmpty() 
                              ? request.getChatRoomName() 
                              : "프로젝트 " + request.getProjectNum() + " 채팅방"; // 기본 이름 설정 가능

        // 채팅방 생성
        ChatRoomEntity chatRoom = ChatRoomEntity.builder()
                .projectNum(request.getProjectNum())
                .chatroomName(chatRoomName) // 전달된 채팅방 이름 설정
                .createdTime(LocalDateTime.now())
                .build();
        ChatRoomEntity savedChatRoom = chatRoomRepository.save(chatRoom);

        // 로그인한 사용자 채팅방 참가자로 추가
        ChatParticipantEntity creatorParticipant = ChatParticipantEntity.builder()
                .chatroomId(savedChatRoom.getChatroomId())
                .participantId(loggedInUserId)
                .joinedTime(LocalDateTime.now())
                .build();
        chatParticipantRepository.save(creatorParticipant);

        // 선택된 멤버들을 추가
        for (String memberId : request.getSelectedMemberIds()) {
            if (!memberId.equals(loggedInUserId)) { // 로그인한 사용자가 중복 추가되지 않도록 체크
                ChatParticipantEntity participant = ChatParticipantEntity.builder()
                        .chatroomId(savedChatRoom.getChatroomId())
                        .participantId(memberId)
                        .joinedTime(LocalDateTime.now())
                        .build();
                chatParticipantRepository.save(participant);
            }
        }
    }

    @Transactional
    public void createChatRoomsForAllMemberProjects(String memberId) {
        List<Integer> projectNums = teamRepository.findByMemberId(memberId).stream()
                .map(TeamEntity::getProjectNum)
                .distinct()
                .collect(Collectors.toList());

        for (Integer projectNum : projectNums) {
            if (!chatRoomRepository.existsByProjectNum(projectNum)) {
                ChatRoomEntity chatRoom = ChatRoomEntity.builder()
                        .projectNum(projectNum)
                        .chatroomName("프로젝트 " + projectNum + " 채팅방")
                        .createdTime(LocalDateTime.now())
                        .build();
                ChatRoomEntity savedChatRoom = chatRoomRepository.save(chatRoom);

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

    @Transactional
    public void saveMessage(ChatMessageDTO chatMessageDTO) {
        chatMessageService.saveMessage(chatMessageDTO);
    }

    public List<ChatMessageDTO> getMessagesForChatRoom(int chatroomId) {
        return chatMessageService.getAllMessagesForChatroom(chatroomId);
    }

    public ChatRoomEntity getChatRoomById(int chatroomId) {
        return chatRoomRepository.findById(chatroomId).orElse(null);
    }
    
    public List<ProjectDTO> getProjectsForMember(String memberId) {
        return teamRepository.findByMemberId(memberId)
                .stream()
                .map(team -> new ProjectDTO(team.getProjectNum(), team.getProject().getProjectName()))
                .collect(Collectors.toList());
    }
    
 // 팀원 목록 가져오기 (현재 사용자 제외)
    public List<String> getTeamMembersByProjectNum(int projectNum, String currentUserId) {
        // 해당 프로젝트에 속한 팀원 목록 가져오기
        List<TeamEntity> teamEntities = teamRepository.findByProjectNum(projectNum);
        
        // 현재 로그인한 사용자를 제외하고 팀원 ID 리스트 반환
        return teamEntities.stream()
                .map(TeamEntity::getMemberId)
                .filter(memberId -> !memberId.equals(currentUserId)) // 현재 사용자를 제외
                .collect(Collectors.toList());
    }
    
 // 현재 로그인된 사용자가 속한 채팅방만 가져오는 메서드
    public List<ChatRoomDTO> getChatRoomsForLoggedInUser(String userId) {
        List<Integer> userChatroomIds = chatParticipantRepository.findByParticipantId(userId)
                .stream()
                .map(ChatParticipantEntity::getChatroomId)
                .distinct()
                .collect(Collectors.toList());

        // 해당 ID에 속하는 채팅방만 조회하고 DTO로 변환하여 반환
        return chatRoomRepository.findAllById(userChatroomIds).stream()
                .map(chatRoom -> ChatRoomDTO.builder()
                        .chatroomId(chatRoom.getChatroomId())
                        .projectNum(chatRoom.getProjectNum())
                        .chatroomName(chatRoom.getChatroomName())
                        .createdTime(Timestamp.valueOf(chatRoom.getCreatedTime())) // LocalDateTime을 Timestamp로 변환
                        .build())
                .collect(Collectors.toList());
    }
    
    public List<String> getParticipantsByChatroomId(int chatroomId) {
        // chatParticipantRepository를 이용하여 채팅방에 참가한 멤버의 아이디를 조회
        return chatParticipantRepository.findByChatroomId(chatroomId)
                .stream()
                .map(ChatParticipantEntity::getParticipantId) // 참가자의 ID를 추출
                .collect(Collectors.toList());
    }
    
    // 프로젝트 번호로 팀 멤버 목록 가져오기 (이미 초대된 멤버 구분)
    public List<String> getAvailableTeamMembers(int projectNum, int chatroomId) {
        // 프로젝트 번호로 팀 멤버 가져오기
        List<String> allMembers = teamRepository.findByProjectNum(projectNum)
                .stream()
                .map(TeamEntity::getMemberId)
                .collect(Collectors.toList());

        // 현재 채팅방에 참가한 멤버 가져오기
        List<String> currentParticipants = chatParticipantRepository.findByChatroomId(chatroomId)
                .stream()
                .map(ChatParticipantEntity::getParticipantId)
                .collect(Collectors.toList());

        // 중복 제거 및 현재 참가자 표시를 위해 Map으로 반환
        return allMembers.stream()
                .map(memberId -> currentParticipants.contains(memberId) ? memberId + " (참가 중)" : memberId)
                .collect(Collectors.toList());
    }
}
