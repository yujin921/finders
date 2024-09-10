package net.datasa.finders.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.datasa.finders.domain.entity.ChatRoomEntity;
import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.domain.entity.ProjectEntity;
import net.datasa.finders.repository.ChatRoomRepository;
import net.datasa.finders.repository.MemberRepository;
import net.datasa.finders.repository.ProjectRepository;

@Service
public class ProjectService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository; // ChatRoomRepository 주입 추가

    @Autowired
    private ChatParticipantService chatParticipantService; // ChatParticipantService 주입

    @Transactional
    public void addMemberToProject(String userId, int projectNum) {
        // 사용자와 프로젝트 엔티티 조회
        MemberEntity member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        ProjectEntity project = projectRepository.findById(projectNum)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다: " + projectNum));

        // 사용자와 프로젝트 간의 관계 설정
        System.out.println("Adding member to project: " + userId + " to project " + projectNum);
        boolean addedToMember = member.getProjects().add(project);
        boolean addedToProject = project.getMembers().add(member);

        // 관계 추가 여부 로그로 확인
        System.out.println("Member added to project set: " + addedToMember);
        System.out.println("Project added to member set: " + addedToProject);

        // 관계 저장
        System.out.println("Saving member-project relationship...");
        memberRepository.save(member);  // 이 단계에서 team 테이블에 데이터가 삽입됩니다.

        // 팀 추가 후 chat_participant 업데이트 로직 추가
        ChatRoomEntity chatRoom = chatRoomRepository.findByProjectNum(projectNum)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트에 해당하는 채팅방을 찾을 수 없습니다: " + projectNum));

        // 채팅방 참여자 추가
        chatParticipantService.addParticipant(chatRoom.getChatroomId(), userId);

        // 로그로 데이터 삽입 확인
        System.out.println("User " + userId + " added to project " + projectNum + " and chat participant.");
    }

    // 프로젝트에 속한 사용자인지 확인하는 메서드
    public boolean isUserAssignedToProject(String userId, int projectNum) {
        List<MemberEntity> members = memberRepository.findByMemberIdAndProjects_ProjectNum(userId, projectNum);
        System.out.println("프로젝트 멤버 조회 결과: " + members);
        return !members.isEmpty();
    }
}
