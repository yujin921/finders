package net.datasa.finders.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.ProjectDTO;
import net.datasa.finders.domain.entity.ApplicationResult;
import net.datasa.finders.domain.entity.ChatRoomEntity;
import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.domain.entity.ProjectApplicationEntity;
import net.datasa.finders.domain.entity.ProjectEntity;
import net.datasa.finders.domain.entity.ProjectManagementEntity;
import net.datasa.finders.domain.entity.ProjectPublishingEntity;
import net.datasa.finders.domain.entity.RoleName;
import net.datasa.finders.repository.ChatRoomRepository;
import net.datasa.finders.repository.MemberRepository;
import net.datasa.finders.repository.ProjectApplicationRepository;
import net.datasa.finders.repository.ProjectManagementRepository;
import net.datasa.finders.repository.ProjectPublishingRepository;
import net.datasa.finders.repository.ProjectRepository;
import net.datasa.finders.repository.TeamRepository;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class ProjectService {
	
    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantService chatParticipantService;
    private final TeamRepository teamRepository;
    private final ProjectApplicationRepository projectApplicationRepository;
    private final ProjectPublishingRepository projectPublishingRepository;
    private final ProjectManagementRepository projectManagementRepository;
 
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
        memberRepository.save(member); // 이 단계에서 team 테이블에 데이터가 삽입됩니다.

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

    public List<ProjectEntity> getProjectsByMemberId(String memberId) {
        try {
            System.out.println("getProjectsByMemberId method called with memberId: " + memberId);
            List<ProjectEntity> projects = projectRepository.findProjectsByMemberId(memberId);
            if (projects == null || projects.isEmpty()) {
                System.out.println("No projects found for member: " + memberId);
            } else {
                projects.forEach(project -> System.out.println("Project ID: " + project.getProjectNum() + ", Name: " + project.getProjectName()));
            }
            return projects;
        } catch (Exception e) {
            System.out.println("Exception occurred: " + e.getMessage());
            e.printStackTrace(); // 예외가 발생한 경우 스택 트레이스를 출력하여 확인합니다.
            return new ArrayList<>();
        }
    }

	public List<ProjectDTO> findProjectsByMemberIdAndStatus(String memberId, String status) {

		MemberEntity memberEntity = memberRepository.findById(memberId).orElseThrow(() -> new EntityNotFoundException("회원정보를 찾을 수 없습니다."));
		ArrayList<ProjectDTO> projectDTOList = new ArrayList<>();
		if(memberEntity.getRoleName() == RoleName.ROLE_FREELANCER) {
			if (status.equals("모집중")) {
				List<ProjectApplicationEntity> projectApplicationEntityList = projectApplicationRepository.findByFreelancerAndApplicationResult(memberEntity, ApplicationResult.PENDING);
				for(ProjectApplicationEntity projectApplicationEntity : projectApplicationEntityList) {
					ProjectDTO projectDTO = ProjectDTO.builder()
							.projectName(projectApplicationEntity.getProjectNum().getProjectTitle())
							.projectNum(projectApplicationEntity.getProjectNum().getProjectNum())
							.build();
					projectDTOList.add(projectDTO);
				}
				
			} else {
				List<ProjectApplicationEntity> projectApplicationEntityList = projectApplicationRepository.findByFreelancerAndApplicationResult(memberEntity, ApplicationResult.ACCEPTED);
				for(ProjectApplicationEntity projectApplicationEntity : projectApplicationEntityList) {
					ProjectManagementEntity projectManagementEntity = projectManagementRepository.findByProjectPublishing(projectApplicationEntity.getProjectNum());
					log.debug("{}", projectManagementEntity);
					if (status.equals("진행중") && projectManagementEntity == null) {
						ProjectDTO projectDTO = ProjectDTO.builder()
								.projectName(projectApplicationEntity.getProjectNum().getProjectTitle())
								.projectNum(projectApplicationEntity.getProjectNum().getProjectNum())
								.build();
						projectDTOList.add(projectDTO);
					} else if (status.equals("완료된") && projectManagementEntity != null) {
						ProjectDTO projectDTO = ProjectDTO.builder()
								.projectName(projectApplicationEntity.getProjectNum().getProjectTitle())
								.projectNum(projectApplicationEntity.getProjectNum().getProjectNum())
								.build();
						projectDTOList.add(projectDTO);
					}
					
				}
			}
		} else {
			if (status.equals("모집중")) {
				List<ProjectPublishingEntity> projectPublishingEntityList = projectPublishingRepository.findByClientIdAndProjectStatus(memberEntity, false);
				for(ProjectPublishingEntity projectPublishingEntity : projectPublishingEntityList) {
					ProjectDTO projectDTO = ProjectDTO.builder()
							.projectName(projectPublishingEntity.getProjectTitle())
							.projectNum(projectPublishingEntity.getProjectNum())
							.build();
					projectDTOList.add(projectDTO);
				}
				
			} else {
				List<ProjectPublishingEntity> projectPublishingEntityList = projectPublishingRepository.findByClientIdAndProjectStatus(memberEntity, true);
				for(ProjectPublishingEntity projectPublishingEntity : projectPublishingEntityList) {
					ProjectManagementEntity projectManagementEntity = projectManagementRepository.findByProjectPublishing(projectPublishingEntity);
					log.debug("{}", projectManagementEntity);
					if (status.equals("진행중") && projectManagementEntity == null) {
						ProjectDTO projectDTO = ProjectDTO.builder()
								.projectName(projectPublishingEntity.getProjectTitle())
								.projectNum(projectPublishingEntity.getProjectNum())
								.build();
						projectDTOList.add(projectDTO);
					} else if (status.equals("완료된") && projectManagementEntity != null) {
						ProjectDTO projectDTO = ProjectDTO.builder()
								.projectName(projectPublishingEntity.getProjectTitle())
								.projectNum(projectPublishingEntity.getProjectNum())
								.build();
						projectDTOList.add(projectDTO);
					}
					
				}
			}
		}
		
		
		return projectDTOList;
	}

	public void applicationDeadline(int projectNum) {
		ProjectPublishingEntity projectPublishingEntity = projectPublishingRepository.findByProjectNum(projectNum);
		projectPublishingEntity.setProjectStatus(true);
		projectPublishingRepository.save(projectPublishingEntity);
	}
    
}
