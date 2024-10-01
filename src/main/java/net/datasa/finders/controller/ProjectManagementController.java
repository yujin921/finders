package net.datasa.finders.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.CalendarEventDTO;
import net.datasa.finders.domain.dto.FunctionDTO;
import net.datasa.finders.domain.dto.FunctionTitleDTO;
import net.datasa.finders.domain.dto.ProjectPublishingDTO;
import net.datasa.finders.domain.dto.TaskDTO;
import net.datasa.finders.domain.dto.TaskManagementDTO;
import net.datasa.finders.domain.dto.TeamDTO;
import net.datasa.finders.domain.entity.RoleName;
import net.datasa.finders.domain.entity.TaskStatus;
import net.datasa.finders.security.AuthenticatedUser;
import net.datasa.finders.service.ProjectManagementService;

/**
 * 거래 게시판 관련 콘트롤러
 */

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("myProject")
public class ProjectManagementController {
	
	private final ProjectManagementService projectManagementService;
	
	@GetMapping("view")
	public String view() {
		return "project/listView";
	}
	
	@ResponseBody
    @GetMapping("projectList")
	public ResponseEntity<?> projectList(@AuthenticationPrincipal AuthenticatedUser user) {
	    List<ProjectPublishingDTO> projectList = projectManagementService.getMyList(user.getUsername(), user.getRoleName());
	    
	    if (projectList.isEmpty()) {
	        return ResponseEntity.ok("참여한 프로젝트가 없습니다.");
	    }
	    
	    return ResponseEntity.ok(projectList);
	}
    
    @GetMapping("management")
	public String read(@RequestParam("projectNum") int pNum
			, Model model
			, @AuthenticationPrincipal AuthenticatedUser user) {
	    try {
            RoleName roleName = RoleName.valueOf(user.getRoleName());

            log.debug("현재 사용자의 역할: {}", roleName);
            log.debug("현재 사용자의 ID: {}", user.getUsername());
            
	        ProjectPublishingDTO projectPublishingDTO = projectManagementService.getBoard(pNum, user.getUsername(), roleName);
	        model.addAttribute("board", projectPublishingDTO);
            model.addAttribute("user", user);
            model.addAttribute("roleName", roleName); // 역할을 모델에 추가
            model.addAttribute("userId", user.getUsername()); // 사용자 ID를 모델에 추가
            
	        return "project/management";
	    } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/myProject/view";
	    }
	}
    
    @ResponseBody
    @GetMapping("freelancersInput")
    public ResponseEntity<List<TeamDTO>> getFreelancers(@RequestParam("projectNum") int projectNum,
                                                        @AuthenticationPrincipal AuthenticatedUser user) {
        try {
            String userRole = user.getRoleName(); // 현재 로그인한 사용자의 역할
            String currentMemberId = user.getUsername(); // 현재 로그인한 프리랜서 ID
            
            List<TeamDTO> freelancers = projectManagementService.getFreelancersByProject(projectNum);
            
            List<TeamDTO> filteredFreelancers;
            if ("ROLE_FREELANCER".equals(userRole)) {
                filteredFreelancers = freelancers.stream()
                        .filter(freelancer -> freelancer.getMemberId().equals(currentMemberId)) // 해당 ID만 반환
                        .collect(Collectors.toList());
            } else {
                filteredFreelancers = freelancers; // 일반 회원의 경우 모든 프리랜서 반환
            }
            
            return ResponseEntity.ok(filteredFreelancers);
        } catch (Exception e) {
            e.printStackTrace(); // 로그에 오류 출력
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @ResponseBody
    @PostMapping("saveFunctionAndTask")
    public ResponseEntity<?> saveFunctionAndTask(
            @RequestParam("projectNum") int projectNum,
            @RequestBody TaskManagementDTO taskDTO) {

        try {
            // FunctionTitleName이 request body에 포함된 경우
            String functionTitleName = taskDTO.getFunctionTitleName();

            // 서비스 계층에서 기능 제목과 업무 데이터 저장 처리
            FunctionTitleDTO savedFunction = projectManagementService.saveFunctionAndTask(projectNum, functionTitleName, taskDTO);

            return ResponseEntity.ok(savedFunction);
        } catch (IllegalArgumentException e) {
            log.error("Validation error: ", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error saving function and task: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving function and task");
        }
    }
    
    @ResponseBody
    @GetMapping("loadFunctionTitles")
    public ResponseEntity<List<FunctionTitleDTO>> loadFunctionTitles(@RequestParam(name = "projectNum") int projectNum) {
        try {
            
        	List<FunctionTitleDTO> functionTitles = projectManagementService.getAllFunctionTitles(projectNum);
            
        	return ResponseEntity.ok(functionTitles);
        
        } catch (Exception e) {
            
        	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        
        }
    }
    
    @ResponseBody
    @GetMapping("getTasks")
    public List<TaskManagementDTO> getTasks(@RequestParam("projectNum") int projectNum) {
        try {
            return projectManagementService.getTasks(projectNum);
        } catch (Exception e) {
            log.error("Error retrieving tasks", e);
            return Collections.emptyList();
        }
    }
    
    // 업무 삭제 요청 처리
    @PostMapping("deleteTask")
    public ResponseEntity<String> deleteTask(@RequestParam("taskId") int taskId) {
        try {
            // 업무 삭제
            boolean isDeleted = projectManagementService.deleteTask(taskId);
            if (isDeleted) {
                // 업무 삭제 후 기능 삭제 여부 체크
                Integer functionTitleId = projectManagementService.getFunctionTitleIdByTaskId(taskId);
                if (functionTitleId != null) {
                    // 해당 기능이 비어 있는지 확인
                    if (projectManagementService.isFunctionEmpty(functionTitleId)) {
                        projectManagementService.deleteFunction(functionTitleId); // 기능 삭제
                    }
                }
                return ResponseEntity.ok("업무가 성공적으로 삭제되었습니다.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("업무를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("서버 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 특정 업무 ID로 업무 정보 조회
    @GetMapping("getTaskById")
    public ResponseEntity<TaskManagementDTO> getTaskById(@RequestParam("taskId") int taskId) {
        TaskManagementDTO task = projectManagementService.getTaskById(taskId);
        return task != null ? ResponseEntity.ok(task) : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // 특정 기능에 해당하는 업무 목록 조회
    @GetMapping("getTasksByFunction")
    public ResponseEntity<List<TaskManagementDTO>> getTasksByFunction(@RequestParam("functionTitleId") int functionTitleId) {
        List<TaskManagementDTO> tasks = projectManagementService.getTasksByFunction(functionTitleId);
        return ResponseEntity.ok(tasks);
    }

    // 기능 삭제 요청 처리
    @PostMapping("deleteFunction")
    public ResponseEntity<String> deleteFunction(@RequestParam("functionTitleId") int functionTitleId) {
        try {
            projectManagementService.deleteFunction(functionTitleId);
            return ResponseEntity.ok("기능이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("기능 삭제에 실패했습니다: " + e.getMessage());
        }
    }
    
    @ResponseBody
    @PostMapping("completeProject")
    public ResponseEntity<String> completeProject(@RequestParam("projectNum") int projectNum) {
        try {
            // 프로젝트 완료 처리 메서드 호출
            projectManagementService.projectCompletion(projectNum);
            // 성공 시 200 OK 응답 반환
            return ResponseEntity.ok("success");
        } catch (EntityNotFoundException e) {
            // 엔티티가 발견되지 않을 경우 404 NOT FOUND 응답과 메시지 반환
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // 다른 예외가 발생할 경우 스택 트레이스 출력 후 500 INTERNAL SERVER ERROR 응답 반환
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("failure");
        }
    }
    
    @ResponseBody
    @GetMapping("checkProjectStatus")
    public ResponseEntity<Map<String, Boolean>> checkProjectStatus(@RequestParam("projectNum") int projectNum) {
        Map<String, Boolean> response = new HashMap<>();
        try {
            boolean completeStatus = projectManagementService.isProjectCompleted(projectNum);
            response.put("completeStatus", completeStatus); // 완료 여부 추가

            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @ResponseBody
    @PostMapping("changeTaskStatus")
    public ResponseEntity<String> changeTaskStatus(@RequestParam("taskId") Integer taskId, @RequestParam("taskStatus") TaskStatus status) {
        try {
            projectManagementService.updateTaskStatus(taskId, status);
            return ResponseEntity.ok("업무 상태가 성공적으로 변경되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("업무 상태 변경 실패: " + e.getMessage());
        }
    }
    
    @ResponseBody
    @GetMapping("getGanttChartData")
    public Map<String, Object> getGanttChartData(@RequestParam("projectNum") int projectNum) {
        // 서비스 메서드를 호출하여 Gantt 차트 데이터를 가져옴
        return projectManagementService.getGanttChartData(projectNum);
    }
    
    @ResponseBody
    @GetMapping("loadEntityNames")
    public ResponseEntity<List<Object>> loadEntityNames(@RequestParam("projectNum") int projectNum,
                                                         @RequestParam("entityType") String entityType) {
        try {
            List<Object> entities = new ArrayList<>();
            switch (entityType.toLowerCase()) {
                case "task":
                    // `getTasksFilter` 메소드가 반환하는 리스트를 `List<Object>`로 변환
                    List<TaskDTO> tasks = projectManagementService.getTasksFilter(projectNum);
                    entities.addAll(tasks);
                    break;
                case "function":
                    // `getFunctions` 메소드가 반환하는 리스트를 `List<Object>`로 변환
                    List<FunctionDTO> functions = projectManagementService.getFunctions(projectNum);
                    entities.addAll(functions);
                    break;
                default:
                    return ResponseEntity.badRequest().body(Collections.emptyList());
            }
            return ResponseEntity.ok(entities);
        } catch (Exception e) {
            // 에러 발생 시 로그에 에러 메시지 기록
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }
    
    @ResponseBody
    @PostMapping("updateProgress")
    public String updateProgress(@RequestParam("entityType") String entityType,
    							 @RequestParam("id") int dbId,
                                 @RequestParam("progressValue") String progressValue) {
        try {
        	projectManagementService.updateProgress(entityType, dbId, progressValue);
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "failure";
        }
    }
    
    // 실제 일정 업데이트 요청 처리
    @ResponseBody
    @PostMapping("updateSchedule")
    public ResponseEntity<String> updateSchedule(
            @RequestParam("entityType") String entityType,
            @RequestParam("id") int dbId,
            @RequestParam("actualStart") String actualStart,
            @RequestParam("actualEnd") String actualEnd) {
    	
        try {
            // 요청 데이터를 Service로 전달
            projectManagementService.updateSchedule(entityType, dbId, actualStart, actualEnd);
            return ResponseEntity.ok("success");
        } catch (IllegalArgumentException e) {
            // 유효하지 않은 EntityType의 경우 400 Bad Request 반환
            return ResponseEntity.badRequest().body("유효하지 않은 Entity 유형입니다: " + entityType);
        } catch (EntityNotFoundException e) {
            // 작업을 찾을 수 없는 경우 404 Not Found 반환
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("작업을 찾을 수 없습니다. ID: " + dbId);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("failure");
        }
    }
    
    // 캘린더에 표시할 업무 조회
    @GetMapping("calendar")
    public ResponseEntity<?> getTasksForCalendar(@RequestParam("projectNum") int projectNum) {
        try {
            List<TaskManagementDTO> tasks = projectManagementService.getTasksForCalendar(projectNum);
            return ResponseEntity.ok(tasks); // 조회된 업무 리스트를 반환
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("유효하지 않은 요청입니다: " + e.getMessage()); // 잘못된 요청 처리
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 프로젝트를 찾을 수 없습니다: " + e.getMessage()); // 프로젝트 미발견 처리
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다: " + e.getMessage()); // 일반 오류 처리
        }
    }
    
    // 캘린더에 표시할 업무 외 일정 조회
    @GetMapping("calendar/events")
    public ResponseEntity<List<CalendarEventDTO>> getExternalEvents(@RequestParam("projectNum") Integer projectNum) {
    	log.debug("Requested projectNum 체크용 : {}", projectNum);
    	
    	try {
            List<CalendarEventDTO> events = projectManagementService.getExternalEventsByProjectNum(projectNum);
            
            log.debug("Fetched events 체크용 : {}" + events);
            
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    // 캘린더 일정 생성
    @ResponseBody
    @PostMapping("calendar/event")
    public ResponseEntity<?> createCalendarEvent(@RequestBody CalendarEventDTO calendarEventDTO) {
        try {
            CalendarEventDTO savedEvent = projectManagementService.createEvent(calendarEventDTO);
            return ResponseEntity.ok(savedEvent);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    // 캘린더 일정 삭제
    @ResponseBody
    @PostMapping("calendar/deleteEvent")
    public ResponseEntity<?> deleteCalendarEvent(@RequestParam("eventId") Integer eventId) {
        try {
            boolean isDeleted = projectManagementService.deleteEvent(eventId);
            if (isDeleted) {
                return ResponseEntity.noContent().build(); // 삭제 성공
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("일정을 찾을 수 없습니다."); // 일정이 존재하지 않음
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    
//    @GetMapping("/project/application-list")
//    public ResponseEntity<List<Application>> getApplicationList(@RequestParam int projectNum) {
//        List<Application> applications = applicationService.getApplicationsByProjectNum(projectNum);  // 해당 projectNum에 맞는 지원자 필터링
//        return ResponseEntity.ok(applications);
//    }

    

    // 임시 리스트 화면 구현 시 기존 프로젝트 생성 페이지 Controller 코드
    /*
    @GetMapping("write")
    public String write() {
        return "project/writeForm";
    }
     
    @PostMapping("write")
    public String write(
            @ModelAttribute ProjectPublishingDTO projectPublishingDTO,
            @RequestParam("projectImageFile") MultipartFile projectImageFile, // 이미지 파일
            @RequestParam("selectedSkills") String selectedSkills,  // 관련 기술
            @RequestParam("projectDescription") String projectDescription,  // 상세 업무 내용
            @RequestParam("projectBudget") BigDecimal projectBudget,  // 지출 예산
            @RequestParam("projectStartDate") LocalDate projectStartDate,  // 프로젝트 시작일
            @RequestParam("projectEndDate") LocalDate projectEndDate,  // 프로젝트 종료일
            @RequestParam("recruitDeadline") LocalDateTime recruitDeadline,
            @RequestParam("role") List<String> roles, // 모집 인원 역할
            @RequestParam("category") List<String> categories, // 모집 인원 카테고리
            @RequestParam("teamSize[]") List<Integer> teamSizes, // 모집 인원
            @RequestParam("question[]") List<String> questions, // 사전 질문
            @AuthenticationPrincipal AuthenticatedUser user) {

        // 작성한 글에 사용자 아이디 추가
        projectPublishingDTO.setClientId(user.getUsername());

        // BoardService 호출해서 프로젝트 및 관련 데이터 저장
        boardService.write(projectPublishingDTO, projectImageFile, selectedSkills
                , projectDescription, projectBudget, projectStartDate, projectEndDate, recruitDeadline, roles, categories, teamSizes, questions);

        return "redirect:/myProject/view";
    }

	@GetMapping("read")
	public String read(@RequestParam("projectNum") int pNum, Model model, @AuthenticationPrincipal AuthenticatedUser user) {
	    try {
            RoleName roleName = RoleName.valueOf(user.getRoleName());

            log.debug("현재 사용자의 역할: {}", roleName);
	        ProjectPublishingDTO projectPublishingDTO = boardService.getBoard(pNum, user.getUsername(), roleName);
	        model.addAttribute("board", projectPublishingDTO);
            model.addAttribute("user", user);
            model.addAttribute("roleName", projectPublishingDTO.getRoleName());
	        return "project/read";
	    } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/myProject/view";
	    }
	}

	@PostMapping("delete")
    public String deletePost(@RequestParam("projectNum") int pNum, @AuthenticationPrincipal AuthenticatedUser user) {
        try {
            RoleName roleName = RoleName.valueOf(user.getRoleName());
            ProjectPublishingDTO projectPublishingDTO = boardService.getBoard(pNum, user.getUsername(), roleName);

            // 로그인된 사용자와 게시글 작성자가 같은지 확인
            if (projectPublishingDTO.getClientId().equals(user.getUsername())) {
                boardService.deleteBoard(pNum);
                return "redirect:/myProject/view"; // 게시글 목록 페이지로 리다이렉트
            } else {
                return "redirect:/myProject/view?error=deletePermission"; // 권한 오류 페이지로 리다이렉트
            }
        } catch (Exception e) {
            return "redirect:/myProject/view?error=deleteFailed"; // 삭제 실패 페이지로 리다이렉트
        }
    }
    */
}
