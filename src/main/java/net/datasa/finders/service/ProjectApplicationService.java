package net.datasa.finders.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.ProjectApplicationDTO;
import net.datasa.finders.domain.dto.TeamDTO;
import net.datasa.finders.domain.entity.*;
import net.datasa.finders.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class ProjectApplicationService {
    private final ProjectPublishingRepository projectPublishingRepository;
    private final ProjectApplicationRepository projectApplicationRepository;
    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;
    private final ProjectRepository projectRepository;

    // 프리랜서가 프로젝트에 이미 지원했는지 확인
    public boolean hasApplied(int projectNum, String freelancerUsername) {
        MemberEntity freelancer = memberRepository.findByCustomMemberId(freelancerUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));
        ProjectPublishingEntity project = projectPublishingRepository.findById(projectNum)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        return projectApplicationRepository.existsByProjectNumAndFreelancer(project, freelancer);
    }

    // 프리랜서의 신청 상태 조회
    public String getApplicationStatus(int projectNum, String freelancerUsername) {
        MemberEntity freelancer = memberRepository.findByCustomMemberId(freelancerUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));
        ProjectPublishingEntity project = projectPublishingRepository.findById(projectNum)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        return projectApplicationRepository.findByProjectNumAndFreelancer(project, freelancer)
                .orElseThrow(() -> new IllegalArgumentException("신청 기록이 없습니다."))
                .getApplicationResult().name();  // 상태를 String으로 반환
    }

    // 신청 정보 저장 (PENDING 상태)
    public void applyToProject(int projectNum, String freelancerUsername) {
        MemberEntity freelancer = memberRepository.findByCustomMemberId(freelancerUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ProjectPublishingEntity project = projectPublishingRepository.findById(projectNum)
                .orElseThrow(() -> new RuntimeException("Project not found"));


        ProjectApplicationEntity application = new ProjectApplicationEntity();
        application.setProjectNum(project);
        application.setFreelancer(freelancer);  // 프리랜서 정보 설정
        application.setApplicationResult(ApplicationResult.PENDING);  // 기본 상태는 PENDING

        projectApplicationRepository.save(application);  // 신청 정보 저장
    }

    // 클라이언트가 상태를 업데이트
    public void updateApplicationStatus(int projectNum, String freelancerUsername, ApplicationResult result) {
        MemberEntity freelancer = memberRepository.findByCustomMemberId(freelancerUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ProjectPublishingEntity projectPublishing = projectPublishingRepository.findById(projectNum)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        ProjectEntity projectEntity = projectRepository.findById(projectNum)
                .orElseThrow(() -> new RuntimeException("ProjectEntity not found"));

        ProjectApplicationEntity application = projectApplicationRepository.findByProjectNumAndFreelancer(projectPublishing, freelancer)
                .orElseThrow(() -> new IllegalArgumentException("신청 기록이 없습니다."));

        application.setApplicationResult(result);  // 상태 업데이트
        projectApplicationRepository.save(application);

        // 상태가 ACCEPTED인 경우 팀에 추가
        if (result == ApplicationResult.ACCEPTED) {
            addMembersToTeam(projectEntity, projectPublishing.getClientId(), freelancer);
        }
    }

    // 팀에 신청자와 작성자 추가하는 메서드
    private void addMembersToTeam(ProjectEntity projectEntity, MemberEntity client, MemberEntity freelancer) {
        // 프리랜서 팀에 추가
        if (!teamRepository.existsByProjectNumAndMemberId(projectEntity.getProjectNum(), freelancer.getMemberId())) {
            TeamEntity teamFreelancer = TeamEntity.builder()
                    .projectNum(projectEntity.getProjectNum())  // 프로젝트 번호
                    .memberId(freelancer.getMemberId())  // 프리랜서 ID
                    .project(projectEntity)
                    .build();
            teamRepository.save(teamFreelancer);
        }

        // 작성자가 이미 팀에 있는지 확인 후 추가
        if (!teamRepository.existsByProjectNumAndMemberId(projectEntity.getProjectNum(), client.getMemberId())) {
            TeamEntity teamAuthor = TeamEntity.builder()
                    .projectNum(projectEntity.getProjectNum())  // 프로젝트 번호
                    .memberId(client.getMemberId())  // 작성자 ID
                    .project(projectEntity)
                    .build();
            teamRepository.save(teamAuthor);
        }
    }

    // 클라이언트가 작성한 프로젝트에 지원한 프리랜서 목록을 조회하는 메서드
    public List<ProjectApplicationDTO> getApplicationsByClient(int projectNum, String clientId) {
        // clientId를 기반으로 Client의 프로젝트 목록을 조회
        MemberEntity client = memberRepository.findByCustomMemberId(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        ProjectPublishingEntity project = projectPublishingRepository.findById(projectNum)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // 클라이언트가 올린 프로젝트에 지원한 프리랜서 목록을 가져옴
        List<ProjectApplicationEntity> applications = projectApplicationRepository.findByProjectNumAndProjectNum_ClientId(project, client);

        // Entity 리스트를 DTO 리스트로 변환
        return applications.stream()
                .map(application -> ProjectApplicationDTO.builder()
                        .applicationNum(application.getApplicationNum())
                        .projectNum(application.getProjectNum().getProjectNum())  // ProjectNum을 DTO에 넣음
                        .projectTitle(application.getProjectNum().getProjectTitle())  // 프로젝트 제목
                        .freelancerId(application.getFreelancer().getMemberId()) // 프리랜서 아이디
                        .applicationResult(application.getApplicationResult())   // 신청 상태
                        .build())
                .collect(Collectors.toList());
    }

    public List<TeamDTO> getTeamMembersByProject(int projectNum) {
        List<TeamEntity> teamEntities = teamRepository.findByProjectNum(projectNum);
        return teamEntities.stream()
                .map(team -> TeamDTO.builder()
                        .projectNum(team.getProjectNum())
                        .memberId(team.getMemberId())
                        .roleName(team.getMember().getRoleName().toString())
                        .build())
                .collect(Collectors.toList());
    }
}
