package net.datasa.finders.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.entity.ApplicationResult;
import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.domain.entity.ProjectApplicationEntity;
import net.datasa.finders.domain.entity.ProjectPublishingEntity;
import net.datasa.finders.repository.MemberRepository;
import net.datasa.finders.repository.ProjectApplicationRepository;
import net.datasa.finders.repository.ProjectPublishingRepository;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class ProjectApplicationService {

    private final ProjectPublishingRepository projectPublishingRepository;
    private final ProjectApplicationRepository projectApplicationRepository;
    private final MemberRepository memberRepository;

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

        ProjectPublishingEntity project = projectPublishingRepository.findById(projectNum)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        ProjectApplicationEntity application = projectApplicationRepository.findByProjectNumAndFreelancer(project, freelancer)
                .orElseThrow(() -> new IllegalArgumentException("신청 기록이 없습니다."));

        application.setApplicationResult(result);  // 상태 업데이트
        projectApplicationRepository.save(application);
    }
}
