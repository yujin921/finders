package net.datasa.finders.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.ProjectApplicationDTO;
import net.datasa.finders.domain.entity.ApplicationResult;
import net.datasa.finders.security.AuthenticatedUser;
import net.datasa.finders.service.ProjectApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("project")
public class ManagementRestController {
    private final ProjectApplicationService projectApplicationService;

    @GetMapping("application-list")
    public ResponseEntity<List<ProjectApplicationDTO>> getApplicationsForClient(@AuthenticationPrincipal AuthenticatedUser user) {
        // 클라이언트가 작성한 프로젝트에 지원한 프리랜서 목록을 가져옴
        List<ProjectApplicationDTO> applications = projectApplicationService.getApplicationsByClient(user.getUsername());
        return ResponseEntity.ok(applications);
    }

    @PostMapping("update-application-status")
    public ResponseEntity<Void> updateApplicationStatus(
            @RequestParam("projectNum") int projectNum,
            @RequestParam("freelancerId") String freelancerId,
            @RequestParam("status") String status) {

        // Enum으로 변환하기
        ApplicationResult result;
        try {
            result = ApplicationResult.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();  // 잘못된 상태값인 경우 400 응답
        }
        // 상태 업데이트
        projectApplicationService.updateApplicationStatus(projectNum, freelancerId, result);
        return ResponseEntity.ok().build();  // 성공적으로 상태가 변경된 경우 200 응답
    }
}
