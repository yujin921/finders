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
import net.datasa.finders.repository.ChatMessageRepository;
import net.datasa.finders.repository.ChatParticipantRepository;
import net.datasa.finders.repository.ChatRoomRepository;
import net.datasa.finders.repository.ProjectRepository;
import net.datasa.finders.repository.TeamRepository;

//채팅전용
@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final TeamRepository teamRepository;
    private final ChatMessageService chatMessageService;
    private final ProjectRepository projectRepository;
    private final ChatMessageRepository chatMessageRepository;

    
    @Autowired
    public ChatRoomService(ChatRoomRepository chatRoomRepository, 
                           ChatParticipantRepository chatParticipantRepository,
                           TeamRepository teamRepository,
                           ChatMessageService chatMessageService,
                           ProjectRepository projectRepository,
                           ChatMessageRepository chatMessageRepository) { // ProjectRepository 추가
        this.chatRoomRepository = chatRoomRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.teamRepository = teamRepository;
        this.chatMessageService = chatMessageService;
        this.projectRepository = projectRepository; 
        this.chatMessageRepository = chatMessageRepository;// 초기화
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
        List<ChatRoomEntity> chatRooms = chatRoomRepository.findAllById(userChatroomIds);

        // map() 메서드에서 정확한 타입을 지정하여 변환
        return chatRooms.stream()
                .map((ChatRoomEntity chatRoom) -> ChatRoomDTO.builder()
                        .chatroomId(chatRoom.getChatroomId())
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
    
    // 멤버 초대 로직
    @Transactional
    public void inviteMember(int chatroomId, String memberId) {
        // 이미 초대된 멤버인지 확인
        boolean isAlreadyParticipant = chatParticipantRepository.existsByChatroomIdAndParticipantId(chatroomId, memberId);
        if (!isAlreadyParticipant) {
            ChatParticipantEntity participant = ChatParticipantEntity.builder()
                    .chatroomId(chatroomId)
                    .participantId(memberId)
                    .joinedTime(LocalDateTime.now())
                    .build();
            chatParticipantRepository.save(participant);
        } else {
            throw new IllegalArgumentException("이미 참가 중인 멤버입니다.");
        }
    }
    
    public Integer findProjectNumByChatroomId(int chatroomId) {
        // chatroomId로 채팅방 엔티티를 찾고 projectNum을 반환합니다.
        return chatRoomRepository.findById(chatroomId)
                .map(ChatRoomEntity::getProjectNum)
                .orElse(null); // 채팅방이 없을 경우 null 반환
    }
    
 // ChatRoomService.java
    public ChatRoomDTO convertToDTO(ChatRoomEntity chatRoomEntity) {
        return ChatRoomDTO.builder()
            .chatroomId(chatRoomEntity.getChatroomId())
            .chatroomName(chatRoomEntity.getChatroomName())
            .createdTime(Timestamp.valueOf(chatRoomEntity.getCreatedTime()))
            .projectTitle(chatRoomEntity.getProject().getProjectName()) // 프로젝트 제목 설정
            .build();
    }
    
    public List<ChatRoomDTO> getAllChatRooms() {
        // ChatRoom과 Project 데이터를 JOIN하여 가져옴
        List<ChatRoomEntity> chatRooms = chatRoomRepository.findAllWithProject();

        // DTO 변환 시 projectTitle을 직접 가져옴
        return chatRooms.stream()
            .map(room -> {
                // 프로젝트 제목을 직접 가져옴
                String projectTitle = room.getProject() != null ? room.getProject().getProjectName() : "제목 없음";
                System.out.println("ChatRoomId: " + room.getChatroomId() + ", ProjectNum: " + room.getProjectNum() + ", ProjectTitle: " + projectTitle);

                return ChatRoomDTO.builder()
                    .chatroomId(room.getChatroomId())
                    .chatroomName(room.getChatroomName())
                    .projectNum(room.getProjectNum())
                    .projectTitle(projectTitle) // 프로젝트 제목 설정
                    .createdTime(Timestamp.valueOf(room.getCreatedTime()))
                    .build();
            })
            .collect(Collectors.toList());
    }

    // 채팅방과 메시지 삭제 메서드
    @Transactional
    public void deleteChatRoomIfNoParticipants(int chatRoomId) {
        // 참가자 수 확인
        int participantCount = chatParticipantRepository.countByChatroomId(chatRoomId);
        
        // 참가자가 0명인 경우 삭제 로직 실행
        if (participantCount == 0) {
            // 먼저 메시지를 삭제합니다.
            chatMessageRepository.deleteByChatroomId(chatRoomId);
            // 그런 다음 채팅방을 삭제합니다.
            chatRoomRepository.deleteById(chatRoomId);
            System.out.println("참가자가 없는 채팅방과 메시지를 삭제했습니다.");
        }
    }




    
}
