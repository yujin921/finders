package net.datasa.finders.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.*;
import net.datasa.finders.domain.entity.*;
import net.datasa.finders.repository.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class ProjectManagementService {

	private final ConcurrentHashMap<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();
    private final TaskNotificationsRepository taskNotificationsRepository;
	private final ProjectPublishingRepository projectPublishingRepository;
    private final MemberRepository memberRepository;
    private final WorkScopeRepository workScopeRepository;
    private final ProjectCategoryRepository categoryRepository;
    private final ProjectRequiredSkillRepository skillRepository;
    private final PrequalificationQuestionRepository prequalificationQuestionRepository;
    private final FunctionTitleRepository functionTitleRepository;
    private final TaskManagementRepository taskManagementRepository;
    private final ProjectManagementRepository projectManagementRepository;
    private final TeamRepository teamRepository;
    private final CalendarEventRepository calendarEventRepository;


    public List<ProjectPublishingDTO> getMyList(String id, String roleName) {
    	Sort sort = Sort.by(Sort.Direction.DESC, "projectNum");
        List<ProjectPublishingEntity> entityList = new ArrayList<>();
        
        if (roleName.equals("ROLE_FREELANCER")) {
            // 프리랜서 ID로 팀 조회
            List<TeamEntity> teamEntities = teamRepository.findByMemberId(id);
            
            // 해당 팀의 프로젝트 번호 목록을 가져옴
            List<Integer> projectNums = teamEntities.stream()
                .map(TeamEntity::getProjectNum)
                .collect(Collectors.toList());

            // 프로젝트 번호를 기준으로 프로젝트 조회
            entityList = projectPublishingRepository.findAllByProjectNumIn(projectNums);
            
        } else if (roleName.equals("ROLE_CLIENT")) {
            // 클라이언트의 경우 자신의 프로젝트만 조회
            entityList = projectPublishingRepository.findAll(sort).stream()
                .filter(entity -> entity.getClientId().getMemberId().equals(id))
                .collect(Collectors.toList());
            
        } else if (roleName.equals("ROLE_ADMIN")) {
            // 관리자는 모든 프로젝트 조회
            entityList = projectPublishingRepository.findAll(sort);
        }

        // DTO 변환
        List<ProjectPublishingDTO> dtoList = new ArrayList<>();

        for (ProjectPublishingEntity entity : entityList) {
            ProjectPublishingDTO dto = ProjectPublishingDTO.builder()
                    .projectNum(entity.getProjectNum())
                    .clientId(entity.getClientId().getMemberId())
                    .projectTitle(entity.getProjectTitle())
                    .recruitDeadline(entity.getRecruitDeadline())
                    .projectStartDate(entity.getProjectStartDate())
                    .projectEndDate(entity.getProjectEndDate())
                    .projectBudget(entity.getProjectBudget())
                    .projectDescription(entity.getProjectDescription())
                    .projectImage(entity.getProjectImage())
                    .projectStatus(entity.getProjectStatus())
                    .build();

            dtoList.add(dto);
        }

        return dtoList;
        
        /*
        Sort sort = Sort.by(Sort.Direction.DESC, "projectNum");
        List<ProjectPublishingEntity> entityList;
        
        entityList = projectPublishingRepository.findAll(sort);
        
        List<ProjectPublishingDTO> dtoList = new ArrayList<>();

        for (ProjectPublishingEntity entity : entityList) {
            if (entity.getClientId().getMemberId().equals(id)) {
            	ProjectPublishingDTO dto = ProjectPublishingDTO.builder()
                        .projectNum(entity.getProjectNum())
                        .clientId(entity.getClientId().getMemberId())
                        .projectTitle(entity.getProjectTitle())
                        .recruitDeadline(entity.getRecruitDeadline())
                        .projectStartDate(entity.getProjectStartDate())
                        .projectEndDate(entity.getProjectEndDate())
                        .projectBudget(entity.getProjectBudget())
                        .projectDescription(entity.getProjectDescription())
                        .projectImage(entity.getProjectImage())
                        .projectStatus(entity.getProjectStatus())
                        .build();
                dtoList.add(dto);
            }
        }
            
        return dtoList;
        */

    }
    
    private ProjectPublishingDTO convertToDTO(ProjectPublishingEntity entity) {
        return ProjectPublishingDTO.builder()
                .projectNum(entity.getProjectNum())
                .clientId(entity.getClientId().getMemberId())
                .projectTitle(entity.getProjectTitle())
                .recruitDeadline(entity.getRecruitDeadline())
                .projectStartDate(entity.getProjectStartDate())
                .projectEndDate(entity.getProjectEndDate())
                .projectBudget(entity.getProjectBudget())
                .projectDescription(entity.getProjectDescription())
                .projectImage(entity.getProjectImage())  // 조회 시 Base64로 저장된 이미지 데이터
                .projectStatus(entity.getProjectStatus())
                .build();
    }
    
    public ProjectPublishingDTO getBoard(int pNum, String memberId, RoleName roleName) {
        ProjectPublishingEntity entity = projectPublishingRepository.findById(pNum)
                .orElseThrow(() -> new EntityNotFoundException("해당 번호의 글이 없습니다."));

        // DTO로 변환 작업
        ProjectPublishingDTO dto = convertToDTO(entity);

        LocalDate projectStartDate = entity.getProjectStartDate();
        LocalDate projectEndDate = entity.getProjectEndDate();
        long estimatedDays = ChronoUnit.DAYS.between(projectStartDate, projectEndDate);

        List<PrequalificationQuestionEntity> questions = prequalificationQuestionRepository.findByProjectPublishingEntity(entity);
        List<ProjectRequiredSkillEntity> skills = skillRepository.findByProjectPublishingEntity(entity);
        List<ProjectCategoryEntity> categories = categoryRepository.findByProjectPublishingEntity(entity);
        List<WorkScopeEntity> workScopes = workScopeRepository.findByProjectPublishingEntity(entity);

        List<String> selectedSkills = skills.stream()
                .map(ProjectRequiredSkillEntity::getSkillText)
                .collect(Collectors.toList());
        List<String> questionTexts = questions.stream()
                .map(PrequalificationQuestionEntity::getQuestionText)
                .collect(Collectors.toList());

        List<Map<String, Object>> matchedOutputs = new ArrayList<>();

        // 카테고리와 업무 범위를 매칭할 때, required_num이 같은 경우 매칭
        for (ProjectCategoryEntity category : categories) {
            for (WorkScopeEntity workScope : workScopes) {
                // required_num이 같은 경우 매칭
                if (category.getRequiredNum() == workScope.getRequiredNum()) {
                    Map<String, Object> output = new HashMap<>();
                    output.put("category", category.getCategory());
                    output.put("workScope", workScope.getWorkType());
                    output.put("requiredNum", category.getRequiredNum());  // 동일한 required_num
                    matchedOutputs.add(output);
                } else {
                    log.debug("매칭되지 않은 required_num: 카테고리 {}, 업무 범위 {}",
                            category.getRequiredNum(), workScope.getRequiredNum());
                }
            }
        }
        MemberEntity member = memberRepository.findByMemberIdAndRoleName(memberId, roleName);
        RoleName role = member.getRoleName();

        dto.setRoleName(role);
        dto.setEstimatedDay(estimatedDays);
        dto.setOutputList(matchedOutputs);
        dto.setSelectedSkills(selectedSkills);
        dto.setPrequalificationQuestions(questionTexts);

        return dto;
    }
    
    @Transactional
    public FunctionTitleDTO saveFunction(int projectNum, String functionTitleName) {
        // 중복 체크
        boolean isDuplicate = taskManagementRepository.existsByProjectPublishingEntity_ProjectNumAndFunctionTitleEntity_TitleName(projectNum, functionTitleName);
        
        if (isDuplicate) {
            throw new IllegalArgumentException("중복된 기능 제목이 존재합니다.");
        }

        // 새로운 기능 저장
        FunctionTitleEntity functionTitleEntity = FunctionTitleEntity.builder()
                .titleName(functionTitleName)
                .functionProcessivity("0%")
                .build();

        functionTitleRepository.save(functionTitleEntity);

        return new FunctionTitleDTO(functionTitleEntity.getFunctionTitleId(), functionTitleEntity.getTitleName(), functionTitleEntity.getFunctionProcessivity());
    }

    @Transactional
    public TaskManagementDTO saveTask(int projectNum, TaskManagementDTO taskDTO) {
        // 프로젝트와 프리랜서, 기능 제목 조회
        ProjectPublishingEntity projectPublishing = projectPublishingRepository.findById(projectNum)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 프로젝트입니다."));

        MemberEntity member = memberRepository.findById(taskDTO.getFreelancerId())
                .orElseThrow(() -> new EntityNotFoundException("프리랜서 회원 아이디가 없습니다."));

        FunctionTitleEntity functionTitle = taskDTO.getFunctionTitleId() != null
                ? functionTitleRepository.findById(taskDTO.getFunctionTitleId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 기능 분류 항목입니다."))
                : null;

        // Enum 타입으로 바로 할당
        TaskStatus status = taskDTO.getTaskStatus();
        TaskPriority priority = taskDTO.getTaskPriority();
        
        // 이미 LocalDateTime으로 변경했으므로, 직접 사용
        LocalDateTime startDate = taskDTO.getTaskStartDate();
        LocalDateTime endDate = taskDTO.getTaskEndDate();

        // 중복 체크
        boolean isDuplicate = taskManagementRepository.existsByProjectPublishingEntity_ProjectNumAndTaskTitle(projectNum, taskDTO.getTaskTitle());
        if (isDuplicate) {
            throw new IllegalArgumentException("중복된 업무 제목이 존재합니다.");
        }

        // 역할 체크
        if (member.getRoleName() != RoleName.ROLE_FREELANCER) {
            throw new IllegalArgumentException("해당 회원은 프리랜서가 아닙니다.");
        }

        // 업무 저장
        TaskManagementEntity taskManagementEntity = TaskManagementEntity.builder()
                .projectPublishingEntity(projectPublishing)
                .memberEntity(member)
                .functionTitleEntity(functionTitle)
                .taskTitle(taskDTO.getTaskTitle())
                .taskDescription(taskDTO.getTaskDescription())
                .taskStatus(status)
                .taskPriority(priority)
                .taskStartDate(startDate)
                .taskEndDate(endDate)
                .taskProcessivity("0%")
                .actualStartDate(taskDTO.getActualStartDate() != null ? taskDTO.getActualStartDate() : startDate)
                .actualEndDate(taskDTO.getActualEndDate() != null ? taskDTO.getActualEndDate() : endDate)
                .build();

        taskManagementRepository.save(taskManagementEntity);

        // taskId를 DTO에 설정
        taskDTO.setTaskId(taskManagementEntity.getTaskId());
        
        return taskDTO;
    }

    @Transactional
    public FunctionTitleWithTaskIdDTO saveFunctionAndTask(int projectNum, String functionTitleName, TaskManagementDTO taskDTO) {
        // 기능 제목이 새로 추가된 경우
        FunctionTitleDTO savedFunction;
        if (taskDTO.getFunctionTitleId() == null || taskDTO.getFunctionTitleId() <= 0) {
            // 새 기능 저장
            savedFunction = saveFunction(projectNum, functionTitleName);
            taskDTO.setFunctionTitleId(savedFunction.getFunctionTitleId());
        } else {
            // 기존 기능 선택
            savedFunction = new FunctionTitleDTO(taskDTO.getFunctionTitleId(), functionTitleName, "0%");
        }

        // 업무 저장
        TaskManagementDTO savedTask = saveTask(projectNum, taskDTO);

        // 새로운 DTO 생성하여 반환
        return new FunctionTitleWithTaskIdDTO(savedFunction, savedTask.getTaskId());
    }
    
    public List<FunctionTitleDTO> getAllFunctionTitles(int projectNum) {
        
    	// 주어진 projectNum에 해당하는 모든 업무를 조회합니다.
        List<TaskManagementEntity> tasks = taskManagementRepository.findByProjectPublishingEntity_ProjectNum(projectNum);
        
        log.debug("Tasks retrieved: {}", tasks);
        
        // 업무에서 중복을 제거한 기능 ID를 추출합니다.
        Set<Integer> functionTitleIds = tasks.stream()
                                             .map(task -> task.getFunctionTitleEntity().getFunctionTitleId())
                                             .collect(Collectors.toSet());
        
        log.debug("Function Title IDs: {}", functionTitleIds);
        
        // 기능 ID를 기반으로 기능 정보를 조회합니다.
        List<FunctionTitleEntity> functionTitles = functionTitleRepository.findAllById(functionTitleIds);
        
        log.debug("Function Titles retrieved: {}", functionTitles);
        
        // FunctionTitleEntity를 FunctionTitleDTO로 변환합니다.
        return functionTitles.stream()
                             .map(entity -> new FunctionTitleDTO(entity.getFunctionTitleId(), entity.getTitleName(), entity.getFunctionProcessivity()))
                             .collect(Collectors.toList());
    	
    }
    
    /*
    // 문자열을 LocalDate로 변환
    private LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + date, e);
        }
    }
    */

    public List<TaskManagementDTO> getTasks(int projectNum) {
        // 프로젝트 번호로 업무를 조회
        List<TaskManagementEntity> tasks = taskManagementRepository.findByProjectPublishingEntity_ProjectNum(projectNum);
        
        // DTO 리스트로 변환
        return tasks.stream()
                .map(task -> {
                    // FunctionTitleEntity가 존재하는 경우 functionTitleId와 functionTitleName 가져오기
                    Integer functionTitleId = (task.getFunctionTitleEntity() != null) ? task.getFunctionTitleEntity().getFunctionTitleId() : null;
                    String functionTitleName = (task.getFunctionTitleEntity() != null) ? task.getFunctionTitleEntity().getTitleName() : null;

                    // MemberEntity가 존재하는 경우 freelancerId 가져오기
                    String freelancerId = (task.getMemberEntity() != null) ? task.getMemberEntity().getMemberId() : null;

                    // TaskManagementDTO 객체 생성
                    return TaskManagementDTO.builder()
                            .taskId(task.getTaskId()) // 업무 ID
                            .projectNum(projectNum) // 프로젝트 번호
                            .freelancerId(freelancerId) // 프리랜서 ID
                            .functionTitleId(functionTitleId) // 기능 제목 ID
                            .functionTitleName(functionTitleName) // 기능 제목 이름
                            .taskTitle(task.getTaskTitle()) // 업무 제목
                            .taskDescription(task.getTaskDescription()) // 업무 설명
                            .taskStatus(task.getTaskStatus()) // 업무 상태 (Enum)
                            .taskPriority(task.getTaskPriority()) // 업무 우선순위 (Enum)
                            .taskStartDate(task.getTaskStartDate()) // 업무 시작 날짜 (LocalDateTime)
                            .taskEndDate(task.getTaskEndDate()) // 업무 종료 날짜 (LocalDateTime)
                            .actualStartDate(task.getActualStartDate()) // 실제 시작 날짜
                            .actualEndDate(task.getActualEndDate()) // 실제 종료 날짜
                            .taskProcessivity(task.getTaskProcessivity()) // 업무 진행도
                            .build();
                })
                .collect(Collectors.toList()); // 변환된 DTO 리스트를 반환
    }

    // ProjectNum을 기준으로 데이터를 가져오는 서비스 메서드
    public void projectCompletion(int projectNum) {
        
        ProjectPublishingEntity project = projectPublishingRepository.findById(projectNum)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 프로젝트입니다."));
        
        /*
        TaskDateRangeDTO taskDateRange = taskManagementRepository.findTaskDateRangeByProjectNum(projectNum)
                .orElseThrow(() -> new EntityNotFoundException("업무가 존재하지 않아 일정 조회가 불가합니다."));
                
        LocalDateTime projectEndDateTime = project.getProjectEndDate().atStartOfDay(); // LocalDateTime으로 변환
        LocalDateTime latestEndDate = taskDateRange.getLatestEndDate(); // 이미 LocalDateTime인 경우

        long delayedDays = ChronoUnit.DAYS.between(projectEndDateTime, latestEndDate);
        */

        ProjectManagementEntity projectManagementEntity = ProjectManagementEntity.builder()
                .projectPublishing(project)
                .completeStatus(true) // 완료 여부
                .build();
        
        projectManagementRepository.save(projectManagementEntity);
    }
    
    // 프로젝트 상태 확인 메서드
    public boolean isProjectCompleted(int projectNum) {
        ProjectPublishingEntity e = projectPublishingRepository.findById(projectNum).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 프로젝트입니다."));
        ProjectManagementEntity project = projectManagementRepository.findByProjectPublishing(e);
        if (project == null) {
        	return false;
        }
        return project.getCompleteStatus(); // 완료 여부 반환
    }
    
    // 업무 상태 변경
    public void updateTaskStatus(Integer taskId, TaskStatus status) {
        TaskManagementEntity task = taskManagementRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("업무를 찾을 수 없습니다."));

        // 상태 변경
        task.setTaskStatus(status);
        
        log.debug("업무 ID!! : " + taskId + ", 변경된 상태!! : " + status); // 로그 추가);
        
        taskManagementRepository.save(task); // 상태 변경 후 저장
    }
	
    public Map<String, Object> getGanttChartData(int projectNum) {
        List<TaskManagementEntity> taskEntities = taskManagementRepository.findByProjectPublishingEntity_ProjectNum(projectNum);

        log.debug("taskEntities: {}", taskEntities);
        log.debug("Task Entities Size: " + taskEntities.size());

        Set<Integer> functionTitleIds = taskEntities.stream()
                .map(task -> task.getFunctionTitleEntity().getFunctionTitleId())
                .collect(Collectors.toSet());

        List<FunctionTitleEntity> functionEntities = functionTitleRepository.findAllById(functionTitleIds);
        log.debug("functionEntities: {}", functionEntities);

        Map<Integer, List<TaskManagementEntity>> functionTasksMap = taskEntities.stream()
                .collect(Collectors.groupingBy(task -> task.getFunctionTitleEntity().getFunctionTitleId()));

        List<Map<String, Object>> functionsData = new ArrayList<>();
        AtomicInteger idCounter = new AtomicInteger(1);

        for (FunctionTitleEntity function : functionEntities) {
            List<Map<String, Object>> children = functionTasksMap.getOrDefault(function.getFunctionTitleId(), Collections.emptyList())
                    .stream()
                    .map(task -> {
                        Map<String, Object> taskData = new HashMap<>();
                        int taskId = idCounter.getAndIncrement();
                        taskData.put("id", taskId);
                        taskData.put("dbId", task.getTaskId());
                        taskData.put("entityType", "task");
                        taskData.put("name", task.getTaskTitle());

                        // 신규 업무 등록 시 actualStart와 actualEnd를 baselineStart와 baselineEnd로 초기화
                        OffsetDateTime startDate = (task.getActualStartDate() != null) 
                            ? task.getActualStartDate().atOffset(ZoneOffset.UTC) 
                            : task.getTaskStartDate().atOffset(ZoneOffset.UTC);
                        OffsetDateTime endDate = (task.getActualEndDate() != null) 
                            ? task.getActualEndDate().atOffset(ZoneOffset.UTC) 
                            : task.getTaskEndDate().atOffset(ZoneOffset.UTC);

                        taskData.put("actualStart", startDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                        taskData.put("actualEnd", endDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

                        taskData.put("baselineStart", task.getTaskStartDate().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                        taskData.put("baselineEnd", task.getTaskEndDate().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                        taskData.put("progressValue", task.getTaskProcessivity());
                        taskData.put("rowHeight", 35);
                        return taskData;
                    }).collect(Collectors.toList());

            LocalDateTime functionStartDate = functionTasksMap.getOrDefault(function.getFunctionTitleId(), Collections.emptyList())
                    .stream().map(TaskManagementEntity::getTaskStartDate).min(LocalDateTime::compareTo).orElse(null);
            LocalDateTime functionEndDate = functionTasksMap.getOrDefault(function.getFunctionTitleId(), Collections.emptyList())
                    .stream().map(TaskManagementEntity::getTaskEndDate).max(LocalDateTime::compareTo).orElse(null);

            OffsetDateTime functionActualStart = functionTasksMap.getOrDefault(function.getFunctionTitleId(), Collections.emptyList())
                    .stream()
                    .map(TaskManagementEntity::getActualStartDate)
                    .filter(Objects::nonNull)
                    .map(ldt -> ldt.atOffset(ZoneOffset.UTC))
                    .min(OffsetDateTime::compareTo)
                    .orElse(null);

            OffsetDateTime functionActualEnd = functionTasksMap.getOrDefault(function.getFunctionTitleId(), Collections.emptyList())
                    .stream()
                    .map(TaskManagementEntity::getActualEndDate)
                    .filter(Objects::nonNull)
                    .map(ldt -> ldt.atOffset(ZoneOffset.UTC))
                    .max(OffsetDateTime::compareTo)
                    .orElse(null);

            Map<String, Object> functionData = new HashMap<>();
            functionData.put("id", idCounter.getAndIncrement());
            functionData.put("dbId", function.getFunctionTitleId());
            functionData.put("entityType", "function");
            functionData.put("name", function.getTitleName());
            
            // functionActualStart와 functionActualEnd가 null인 경우 baseline 값을 사용
            functionData.put("actualStart", functionActualStart != null 
                ? functionActualStart.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) 
                : (functionStartDate != null 
                    ? functionStartDate.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) 
                    : null));
            
            functionData.put("actualEnd", functionActualEnd != null 
                ? functionActualEnd.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) 
                : (functionEndDate != null 
                    ? functionEndDate.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) 
                    : null));

            functionData.put("progressValue", function.getFunctionProcessivity());
            functionData.put("baselineStart", functionStartDate != null ? functionStartDate.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null);
            functionData.put("baselineEnd", functionEndDate != null ? functionEndDate.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null);
            functionData.put("rowHeight", 35);
            functionData.put("children", children);

            functionsData.add(functionData);
        }

        functionsData.sort((f1, f2) -> {
            OffsetDateTime start1 = OffsetDateTime.parse((String) f1.get("baselineStart"));
            OffsetDateTime start2 = OffsetDateTime.parse((String) f2.get("baselineStart"));
            return start1.compareTo(start2);
        });

        for (Map<String, Object> functionData : functionsData) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> children = (List<Map<String, Object>>) functionData.get("children");
            children.sort((t1, t2) -> {
                OffsetDateTime start1 = OffsetDateTime.parse((String) t1.get("baselineStart"));
                OffsetDateTime start2 = OffsetDateTime.parse((String) t2.get("baselineStart"));
                return start1.compareTo(start2);
            });
        }

        int bufferDay = 30;

        OffsetDateTime overallStartDate = functionsData.stream()
                .map(f -> (String) f.get("baselineStart"))
                .filter(Objects::nonNull)
                .map(OffsetDateTime::parse)
                .min(OffsetDateTime::compareTo)
                .orElse(null);

        OffsetDateTime overallEndDate = functionsData.stream()
                .map(f -> (String) f.get("baselineEnd"))
                .filter(Objects::nonNull)
                .map(OffsetDateTime::parse)
                .max(OffsetDateTime::compareTo)
                .orElse(null);

        if (overallStartDate != null) {
            overallStartDate = overallStartDate.minusDays(bufferDay);
        }

        if (overallEndDate != null) {
            overallEndDate = overallEndDate.plusDays(bufferDay);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("data", functionsData);
        response.put("adjustedStartDate", overallStartDate != null ? overallStartDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null);
        response.put("adjustedEndDate", overallEndDate != null ? overallEndDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null);
        return response;
    }

	// FunctionTitleDTO를 사용하여 'function'과 'task' 이름을 가져오는 메서드
	public List<FunctionTitleDTO> loadEntityNames(int projectNum, String entityType) {
	    // 모든 TaskManagementEntity를 가져옴
	    List<TaskManagementEntity> tasks = taskManagementRepository.findByProjectPublishingEntity_ProjectNum(projectNum);

	    // FunctionTitleEntity ID 수집
	    Set<Integer> functionTitleIds = tasks.stream()
	        .map(task -> task.getFunctionTitleEntity().getFunctionTitleId())
	        .collect(Collectors.toSet());

	    // FunctionTitleEntity 가져옴
	    List<FunctionTitleEntity> functionTitles = functionTitleRepository.findAllById(functionTitleIds);

	    // entityType에 따라 필터링
	    if ("function".equalsIgnoreCase(entityType)) {
	        // 'function' 선택 시 기능 제목만 반환
	        return functionTitles.stream()
	            .map(function -> new FunctionTitleDTO(function.getFunctionTitleId(), function.getTitleName(), function.getFunctionProcessivity()))
	            .collect(Collectors.toList());
	    } else if ("task".equalsIgnoreCase(entityType)) {
	        // 'task' 선택 시 업무 제목만 반환
	        return tasks.stream()
	            .map(task -> new FunctionTitleDTO(task.getFunctionTitleEntity().getFunctionTitleId(), task.getFunctionTitleEntity().getTitleName(), task.getFunctionTitleEntity().getFunctionProcessivity()))
	            .distinct()
	            .collect(Collectors.toList());
	    } else {
	        throw new IllegalArgumentException("Invalid entityType: " + entityType);
	    }
	}

	public void updateProgress(String entityType, int dbId, String progressValue) {
		log.debug("entityType check용: {}", entityType);
		log.debug("dbId check용: {}", dbId);
		log.debug("progressValue check용: {}", progressValue);
		
        if ("task".equalsIgnoreCase(entityType)) {
            // TaskManagementEntity 처리
            TaskManagementEntity task = taskManagementRepository.findById(dbId)
                .orElseThrow(() -> new RuntimeException("작업을 찾을 수 없습니다."));

            // 진행도 값 업데이트
            task.setTaskProcessivity(progressValue);

            // 작업 저장
            taskManagementRepository.save(task);
        } else if ("function".equalsIgnoreCase(entityType)) {
            // FunctionTitleEntity 처리
            FunctionTitleEntity functionTitle = functionTitleRepository.findById(dbId)
                .orElseThrow(() -> new RuntimeException("기능을 찾을 수 없습니다."));

            // 진행도 값 업데이트
            functionTitle.setFunctionProcessivity(progressValue);

            // 기능 저장
            functionTitleRepository.save(functionTitle);
        } else {
        	throw new IllegalArgumentException("유효하지 않은 Entity 유형입니다. EntityType: " + entityType);
        }
    }
	
	/**
     * 프로젝트 번호를 기준으로 작업(Task) 목록을 조회합니다.
     * 
     * @param projectNum  프로젝트 번호
     * @return            작업 목록
     */
    public List<TaskDTO> getTasksFilter(int projectNum) {
        List<TaskManagementEntity> tasks = taskManagementRepository.findByProjectPublishingEntity_ProjectNum(projectNum);
        return tasks.stream()
            .map(task -> new TaskDTO(task.getTaskId(), task.getTaskTitle()))
            .collect(Collectors.toList());
    }
    
    /**
     * 프로젝트 번호를 기준으로 기능(Function) 목록을 조회합니다.
     * 
     * @param projectNum  프로젝트 번호
     * @return            기능 목록
     */
    public List<FunctionDTO> getFunctions(int projectNum) {
        // 프로젝트 번호를 기준으로 TaskManagementEntity를 조회
        List<TaskManagementEntity> tasks = taskManagementRepository.findByProjectPublishingEntity_ProjectNum(projectNum);

        // TaskManagementEntity에서 FunctionTitleEntity를 추출하고 중복 제거
        List<FunctionTitleEntity> functionTitles = tasks.stream()
            .map(TaskManagementEntity::getFunctionTitleEntity)
            .filter(Objects::nonNull) // FunctionTitleEntity가 null이 아닌 경우만 필터링
            .distinct() // 중복 제거
            .collect(Collectors.toList());

        // FunctionTitleEntity를 FunctionDTO로 변환
        return functionTitles.stream()
            .map(function -> new FunctionDTO(function.getFunctionTitleId(), function.getTitleName()))
            .collect(Collectors.toList());
    }
    
    // 업무 삭제 메소드
    public boolean deleteTask(int taskId) {
        if (taskManagementRepository.existsById(taskId)) {
            taskManagementRepository.deleteById(taskId);
            return true; // 삭제 성공
        }
        return false; // 삭제할 업무가 없음
    }

    // 특정 업무 ID로 기능 ID 조회
    public Integer getFunctionTitleIdByTaskId(int taskId) {
        return taskManagementRepository.findFunctionTitleIdByTaskId(taskId); // 업무에 해당하는 기능 ID 반환
    }

    // 특정 기능에 업무가 남아 있는지 확인
    public boolean isFunctionEmpty(int functionTitleId) {
        return taskManagementRepository.countTasksByFunctionTitleId(functionTitleId) == 0; // 해당 기능에 업무가 남아 있는지 확인
    }

    // 특정 업무 ID로 업무 정보 조회
    public TaskManagementDTO getTaskById(int taskId) {
        return taskManagementRepository.findById(taskId)
                .map(task -> new TaskManagementDTO(
                    task.getTaskId(),
                    task.getProjectPublishingEntity().getProjectNum(),
                    task.getMemberEntity().getMemberId(),
                    task.getFunctionTitleEntity().getFunctionTitleId(),
                    task.getFunctionTitleEntity().getTitleName(),
                    task.getTaskTitle(),
                    task.getTaskDescription(),
                    task.getTaskStatus(),
                    task.getTaskPriority(),
                    task.getTaskStartDate(),
                    task.getTaskEndDate(),
                    task.getActualStartDate(),
                    task.getActualEndDate(),
                    task.getTaskProcessivity()
                ))
                .orElse(null); // 업무 정보 반환
    }

    // 특정 기능에 해당하는 업무 목록 조회
    public List<TaskManagementDTO> getTasksByFunction(int functionTitleId) {
        List<TaskManagementEntity> tasks = taskManagementRepository.findTasksByFunctionTitleId(functionTitleId);
        return tasks.stream()
                    .map(task -> new TaskManagementDTO(
                        task.getTaskId(),
                        task.getProjectPublishingEntity().getProjectNum(),
                        task.getMemberEntity().getMemberId(),
                        task.getFunctionTitleEntity().getFunctionTitleId(),
                        task.getFunctionTitleEntity().getTitleName(),
                        task.getTaskTitle(),
                        task.getTaskDescription(),
                        task.getTaskStatus(),
                        task.getTaskPriority(),
                        task.getTaskStartDate(),
                        task.getTaskEndDate(),
                        task.getActualStartDate(),
                        task.getActualEndDate(),
                        task.getTaskProcessivity()
                    ))
                    .collect(Collectors.toList());
    }
    
    // 기능 삭제 메소드
    public void deleteFunction(int functionTitleId) {
        if (functionTitleRepository.existsById(functionTitleId)) {
            functionTitleRepository.deleteById(functionTitleId);
        } else {
            throw new IllegalArgumentException("기능이 존재하지 않습니다.");
        }
    }

    // 실제 일정 업데이트
    public void updateSchedule(String entityType, int dbId, String actualStartDateStr, String actualEndDateStr) {
        log.debug("entityType check용: {}", entityType);
        log.debug("dbId check용: {}", dbId);
        log.debug("actualStartDateStr check용: {}", actualStartDateStr);
        log.debug("actualEndDateStr check용: {}", actualEndDateStr);

        // 공백 제거 및 소문자로 변환
        entityType = entityType.trim().toLowerCase();
        
        if (!"task".equals(entityType)) {
            throw new IllegalArgumentException("유효하지 않은 Entity 유형입니다. EntityType: " + entityType);
        }

        // 문자열을 OffsetDateTime으로 변환
        OffsetDateTime actualStart = parseOffsetDateTime(actualStartDateStr);
        OffsetDateTime actualEnd = parseOffsetDateTime(actualEndDateStr);

        // TaskManagementEntity 처리
        TaskManagementEntity task = taskManagementRepository.findById(dbId)
            .orElseThrow(() -> new EntityNotFoundException("작업을 찾을 수 없습니다. ID: " + dbId));

        // 실제 시작일 및 종료일 업데이트
        task.setActualStartDate(actualStart.toLocalDateTime());
        task.setActualEndDate(actualEnd.toLocalDateTime());

        // 작업 저장
        taskManagementRepository.save(task);
    }

    // OffsetDateTime으로 변환하는 메서드
    private OffsetDateTime parseOffsetDateTime(String dateStr) {
        try {
            // 문자열을 OffsetDateTime으로 파싱하는 로직
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");
            return OffsetDateTime.parse(dateStr, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("날짜 형식이 올바르지 않습니다: " + dateStr);
        }
    }

    // 프로젝트 번호에 따라 업무를 조회하고 DTO로 변환하여 반환
    public List<TaskManagementDTO> getTasksForCalendar(int projectNum) {
        List<TaskManagementEntity> tasks = taskManagementRepository.findByProjectPublishingEntity_ProjectNum(projectNum);

        return tasks.stream()
                .map(task -> TaskManagementDTO.builder()
                        .taskId(task.getTaskId())
                        .taskTitle(task.getTaskTitle())
                        .taskStartDate(task.getActualStartDate()) // LocalDateTime 그대로 사용
                        .taskEndDate(task.getActualEndDate()) // LocalDateTime 그대로 사용
                        .taskStatus(task.getTaskStatus()) // Enum 그대로 사용
                        .taskPriority(task.getTaskPriority()) // Enum 그대로 사용
                        .build())
                .collect(Collectors.toList());
    }
    
    public CalendarEventDTO createEvent(CalendarEventDTO calendarEventDTO) {
        CalendarEventEntity event = new CalendarEventEntity();;
        event.setTitle(calendarEventDTO.getTitle());
        event.setStartDate(calendarEventDTO.getStartDate());
        event.setEndDate(calendarEventDTO.getEndDate());
        event.setEventType(calendarEventDTO.getEventType());

        // 프로젝트 번호로 프로젝트 조회
        ProjectPublishingEntity project = projectPublishingRepository.findById(calendarEventDTO.getProjectNum())
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));

        event.setProject(project); // 프로젝트 설정
        CalendarEventEntity savedEvent = calendarEventRepository.save(event);
        return new CalendarEventDTO(savedEvent.getEventId(), savedEvent.getTitle(), savedEvent.getStartDate(), savedEvent.getEndDate(), savedEvent.getEventType(), savedEvent.getProject().getProjectNum());
    }

    // 업무등록 모달창의 "담당자" 입력란에 대한 프로젝트 참여하는 프리랜서 ID로 자동완성
    public List<TeamDTO> getFreelancersByProject(int projectNum) {
        List<TeamEntity> teamEntities = teamRepository.findByProjectNum(projectNum);
        
        // Entity를 DTO로 변환, 역할 이름 포함
        return teamEntities.stream()
                .map(entity -> {
                    MemberEntity member = memberRepository.findById(entity.getMemberId())
                            .orElseThrow(() -> new RuntimeException("Member not found"));
                    return new TeamDTO(entity.getTeamNum(), entity.getProjectNum(), member.getMemberId(), member.getRoleName().name());
                })
                .collect(Collectors.toList());
    }

	public List<CalendarEventDTO> getExternalEventsByProjectNum(int projectNum) {
		log.debug("Fetching events for projectNum 체크용 : {}", projectNum);
		
		List<CalendarEventEntity> events = calendarEventRepository.findByProject_ProjectNum(projectNum);
		
		log.debug("Events fetched from repository 체크용 : {}", events);
		
		List<CalendarEventDTO> eventDTOs = new ArrayList<>();

		for (CalendarEventEntity event : events) {
		    CalendarEventDTO dto = CalendarEventDTO.builder()
		        .eventId(event.getEventId())
		        .title(event.getTitle())
		        .startDate(event.getStartDate())
		        .endDate(event.getEndDate())
		        .eventType(event.getEventType())
		        .projectNum(event.getProject().getProjectNum())
		        .build();
		    
		    eventDTOs.add(dto);
		}

		return eventDTOs;

	}

	// 일정 삭제 메소드
    public boolean deleteEvent(Integer eventId) {
        if (calendarEventRepository.existsById(eventId)) {
            calendarEventRepository.deleteById(eventId);
            return true; // 삭제 성공
        }
        return false; // 삭제할 일정이 없음
    }
    
    // 업무 알림 관련 메소드
    public SseEmitter subscribe(String loginId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitterMap.put(loginId, emitter);

        emitter.onCompletion(() -> emitterMap.remove(loginId));
        emitter.onTimeout(() -> emitterMap.remove(loginId));

        return emitter;
    }

    public TaskNotificationsDTO sendMessage(TaskNotificationsDTO notificationDTO) {
    	log.debug("전송할 DTO!! : {}", notificationDTO);
    	
    	// 입력값 검증
        if (notificationDTO.getSender() == null) {
            throw new IllegalArgumentException("보낸 회원의 아이디가 null입니다.");
        }
        if (notificationDTO.getRecipient() == null) {
            throw new IllegalArgumentException("받는 회원의 아이디가 null입니다.");
        }
        if (notificationDTO.getTask() == null) {
            throw new IllegalArgumentException("업무 ID가 null입니다.");
        }
    	
        // 프리랜서 회원의 MemberEntity를 가져옴
        MemberEntity sender = memberRepository.findById(notificationDTO.getSender())
                .orElseThrow(() -> new EntityNotFoundException("보낸 회원의 아이디가 없습니다."));

        // 수신 회원의 MemberEntity를 가져옴
        MemberEntity recipient = memberRepository.findById(notificationDTO.getRecipient())
                .orElseThrow(() -> new EntityNotFoundException("받는 회원의 아이디가 없습니다."));

        // 업무 ID를 가져오고 엔티티를 설정
        TaskManagementEntity task = taskManagementRepository.findById(notificationDTO.getTask())
                .orElseThrow(() -> new EntityNotFoundException("업무를 찾을 수 없습니다."));
        
        // DTO를 엔티티로 변환하여 저장
        TaskNotificationsEntity notification = TaskNotificationsEntity.builder()
                .notificationMessage(notificationDTO.getNotificationMessage())
                .readStatus(false) // 기본값 설정
                .sender(sender) // 가져온 MemberEntity 설정
                .recipient(recipient) // 가져온 MemberEntity 설정
                .task(task)
                .taskDelId(task.getTaskId())
                .projectNum(task.getProjectPublishingEntity().getProjectNum())
                .createDate(LocalDateTime.now()) // 현재 시간 설정
                .build();

        TaskNotificationsEntity savedNotification = taskNotificationsRepository.save(notification);

        // 로그 추가
        log.debug("알림 저장 완료: {}", savedNotification);
        log.debug("저장된 알림 ID: {}", savedNotification.getNotificationId());
        
        // SseEmitter 전송
        SseEmitter emitter = emitterMap.get(notificationDTO.getRecipient());
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("message").data(
                    String.format("{\"message\":\"%s\", \"notificationId\":%d}", notification.getNotificationMessage(), savedNotification.getNotificationId())
                ));
                log.info("알림 전송 성공: {}", notification.getNotificationMessage());
            } catch (IOException e) {
                log.error("메시지 전송 중 오류 발생: ", e);
            }
        } else {
            log.warn("프리랜서 {}에 대한 SseEmitter가 등록되어 있지 않습니다.", notificationDTO.getRecipient());
        }

        return TaskNotificationsDTO.fromEntity(savedNotification); // 알림 DTO 반환
    }

    public Map<String, List<TaskNotificationsDTO>> getNotifications(String recipientId, Integer projectNum) {
    	MemberEntity recipient = memberRepository.findById(recipientId)
                .orElseThrow(() -> new EntityNotFoundException("받는 회원의 아이디가 없습니다."));

        List<TaskNotificationsEntity> notifications = taskNotificationsRepository.findByRecipient(recipient);

        // 모든 알림을 가져와 projectNum에 해당하는 알림만 필터링
        List<TaskNotificationsEntity> filteredNotifications = notifications.stream()
                .filter(notification -> {
                    Integer projectNumFromNotification = notification.getProjectNum();
                    TaskManagementEntity task = notification.getTask();

                    // task가 null이 아니면서 projectNum이 일치하거나,
                    // task가 null인 경우 projectNum만 일치하는 경우 포함
                    return (task != null && projectNumFromNotification != null &&
                            projectNumFromNotification.equals(projectNum)) || 
                           (task == null && projectNumFromNotification != null && 
                            projectNumFromNotification.equals(projectNum));
                })
                .collect(Collectors.toList());

        // 읽음과 안 읽음 알림을 구분
        List<TaskNotificationsDTO> unreadNotifications = filteredNotifications.stream()
                .filter(notification -> !notification.isReadStatus())
                .map(notification -> mapToDTO(notification, recipient))
                .collect(Collectors.toList());

        List<TaskNotificationsDTO> readNotifications = filteredNotifications.stream()
                .filter(TaskNotificationsEntity::isReadStatus)
                .map(notification -> mapToDTO(notification, recipient))
                .collect(Collectors.toList());

        // 결과를 Map으로 반환
        Map<String, List<TaskNotificationsDTO>> result = new HashMap<>();
        result.put("unread", unreadNotifications);
        result.put("read", readNotifications);
        
        return result;
    }

    // DTO 매핑을 위한 메서드 추가
    private TaskNotificationsDTO mapToDTO(TaskNotificationsEntity notification, MemberEntity recipient) {
        TaskNotificationsDTO dto = new TaskNotificationsDTO();
        dto.setNotificationId(notification.getNotificationId());
        dto.setNotificationMessage(notification.getNotificationMessage());
        dto.setReadStatus(notification.isReadStatus());
        dto.setSender(notification.getSender().getMemberId());
        dto.setRecipient(recipient.getMemberId());
        
        // task가 null일 경우 taskDelId를 설정하는 로직
        TaskManagementEntity task = notification.getTask();
        if (task != null) {
            dto.setTask(task.getTaskId()); // task가 null이 아닐 경우 taskId 설정
            dto.setTaskDelId(task.getTaskId()); // taskDelId도 taskId로 설정
            dto.setProjectNum(task.getProjectPublishingEntity().getProjectNum());
        } else {
            dto.setTask(null); // task가 null인 경우
            dto.setTaskDelId(notification.getTaskDelId()); // taskDelId는 notification에서 가져오기
            dto.setProjectNum(notification.getProjectNum());
        }
        
        dto.setCreateDate(notification.getCreateDate());
        return dto;
    }
    
    public void markNotificationAsRead(int notificationId) {
        TaskNotificationsEntity notification = taskNotificationsRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("알림이 없습니다."));

        notification.setReadStatus(true); // 읽음으로 설정
        taskNotificationsRepository.save(notification);
    }
    
    // 기업 회원에게 알림 전송
    public void sendNotificationToClient(String message, int taskId, String userId) {
        // 기존 로직과 동일하게 알림 전송
        TaskNotificationsEntity notification = new TaskNotificationsEntity();
        notification.setNotificationMessage(message);
        notification.setReadStatus(false);

        // 알림을 보내는 회원 정보 (프리랜서 회원 ID 사용)
        MemberEntity sender = memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        notification.setSender(sender);

        // 수신자 정보 가져오기 (업무 ID로 수신자 찾기)
        TaskManagementEntity task = taskManagementRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("업무를 찾을 수 없습니다."));
        
        // 업무 ID 설정
        notification.setTask(task);
        notification.setTaskDelId(task.getTaskId());
        notification.setProjectNum(task.getProjectPublishingEntity().getProjectNum());

        // 프로젝트 정보를 통해 클라이언트 ID 가져오기
        String clientId = task.getProjectPublishingEntity().getClientId().getMemberId();
        MemberEntity recipient = memberRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("클라이언트를 찾을 수 없습니다."));
        notification.setRecipient(recipient);
        
        log.debug("notification 저장 체크용~!! : " + notification);
        
        log.debug("Sending notification to client Message: " + message + ", Task ID: " + taskId + ", Sender ID: " + userId);
        log.debug("Recipient ID: " + clientId);
        
        // 알림 저장
        taskNotificationsRepository.save(notification);
    }

	public String getTaskTitle(int taskId) {
		TaskManagementEntity task = taskManagementRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("업무를 찾을 수 없습니다."));
		
        return task.getTaskTitle();
	}

	// 프리랜서에게 피드백 알림 전송
    public void sendNotificationToFreelancer(String message, int taskId, String userId) {
        // 알림 생성 로직
        TaskNotificationsEntity notification = new TaskNotificationsEntity();
        notification.setNotificationMessage(message);
        notification.setReadStatus(false);

        // 알림을 보내는 회원 정보 (프리랜서 ID 사용)
        MemberEntity sender = memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("프리랜서를 찾을 수 없습니다."));
        notification.setSender(sender);

        // 수신자 정보 가져오기 (업무 ID로 수신자 찾기)
        TaskManagementEntity task = taskManagementRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("업무를 찾을 수 없습니다."));

        // 프로젝트 정보를 통해 프리랜서 ID 가져오기
        String freelancerId = task.getMemberEntity().getMemberId(); // 프리랜서 ID를 직접 가져오는 로직이 필요합니다.
        MemberEntity recipient = memberRepository.findById(freelancerId)
                .orElseThrow(() -> new EntityNotFoundException("프리랜서를 찾을 수 없습니다."));
        notification.setRecipient(recipient);

        // 업무 ID 설정
        notification.setTask(task);
        notification.setTaskDelId(task.getTaskId());
        notification.setProjectNum(task.getProjectPublishingEntity().getProjectNum());
        
        // 알림 저장
        taskNotificationsRepository.save(notification);
    }

	public String getTaskStatus(int taskId) {
		// 업무 ID에 해당하는 업무를 조회
		TaskManagementEntity task = taskManagementRepository.findById(taskId)
            .orElseThrow(() -> new EntityNotFoundException("업무를 찾을 수 없습니다."));
        return task.getTaskStatus().toString(); // 업무 상태 반환
	}
	
	// 업무 삭제 요청
	public TaskResponseDTO requestDeleteTask(Integer taskId, String reason) {
		TaskManagementEntity task = taskManagementRepository.findById(taskId)
	            .orElseThrow(() -> new EntityNotFoundException("업무를 찾을 수 없습니다."));

        // 업무 상태를 "DELETED_REQUEST"로 변경
        task.setTaskStatus(TaskStatus.DELETED_REQUEST);
        taskManagementRepository.save(task); // 상태 업데이트

        // 알림 메시지 생성 및 저장
        String notificationMessage = task.getMemberEntity().getMemberId() + "(이)가" + task.getTaskTitle() + " 업무 삭제를 요청하였습니다. <br>사유: " + reason;

        TaskNotificationsEntity notification = new TaskNotificationsEntity();
        notification.setNotificationMessage(notificationMessage);
        notification.setSender(task.getMemberEntity()); // 요청한 프리랜서 ID

        // ProjectPublishingEntity에서 clientId 가져오기
        MemberEntity client = task.getProjectPublishingEntity().getClientId();
        notification.setRecipient(client); // 기업 회원 ID 설정

        notification.setTask(task);
        notification.setTaskDelId(task.getTaskId());
        notification.setProjectNum(task.getProjectPublishingEntity().getProjectNum());
        taskNotificationsRepository.save(notification);

        // 응답 객체 생성
        return TaskResponseDTO.builder()
                .taskId(taskId)
                .taskTitle(task.getTaskTitle())
                .build();
    }
	
	// 업무 삭제 승인 처리
    public TaskResponseDTO approveDeleteTask(Integer taskId) {
        TaskManagementEntity task = taskManagementRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("업무를 찾을 수 없습니다."));

        // 알림 메시지 생성 및 저장
        String notificationMessage = task.getTaskTitle() + " 업무의 삭제 요청이 승인되어 최종 삭제되었습니다.";

        TaskNotificationsEntity notification = new TaskNotificationsEntity();
        notification.setNotificationMessage(notificationMessage);
        
        // ProjectPublishingEntity에서 clientId 가져오기
        MemberEntity client = task.getProjectPublishingEntity().getClientId();
        notification.setSender(client); // 알림을 보내는 client ID
        notification.setRecipient(task.getMemberEntity()); // 알림을 받는 프리랜서 ID

        notification.setTask(task);
        notification.setTaskDelId(task.getTaskId());
        notification.setProjectNum(task.getProjectPublishingEntity().getProjectNum());
        taskNotificationsRepository.save(notification);
        
        // 응답 객체 생성
        TaskResponseDTO taskResponse = new TaskResponseDTO();
        taskResponse.setTaskId(taskId);
        taskResponse.setTaskTitle(task.getTaskTitle());
        
        // functionTitleId 저장
        taskResponse.setFunctionTitleId(task.getFunctionTitleEntity().getFunctionTitleId()); // DTO에 functionTitleId 설정
        
        // 상태를 DELETED_APPROVED로 변경
        task.setTaskStatus(TaskStatus.DELETED_APPROVED);
        taskManagementRepository.delete(task); // 업무 삭제
        
        return taskResponse;
    }

    // 업무 삭제 거부 처리
    public TaskResponseDTO denyDeleteTask(Integer taskId, String reason) {
        TaskManagementEntity task = taskManagementRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("업무를 찾을 수 없습니다."));
        
        // 상태를 DELETED_DENIED로 변경
        task.setTaskStatus(TaskStatus.DELETED_DENIED);
        taskManagementRepository.save(task); // 상태 업데이트

        // 알림 메시지 생성 및 저장
        String notificationMessage = task.getMemberEntity().getMemberId() + "(이)가 " + task.getTaskTitle() + " 업무 삭제 요청이 거절되었습니다. <br>사유: " + reason;

        TaskNotificationsEntity notification = new TaskNotificationsEntity();
        notification.setNotificationMessage(notificationMessage);
        
        // ProjectPublishingEntity에서 clientId 가져오기
        MemberEntity client = task.getProjectPublishingEntity().getClientId();
        notification.setSender(client); // 알림을 보내는 client ID
        notification.setRecipient(task.getMemberEntity()); // 알림을 받는 프리랜서 ID

        notification.setTask(task);
        notification.setTaskDelId(task.getTaskId());
        notification.setProjectNum(task.getProjectPublishingEntity().getProjectNum());
        taskNotificationsRepository.save(notification);

        // 응답 객체 생성
        return TaskResponseDTO.builder()
                .taskId(taskId)
                .taskTitle(task.getTaskTitle())
                .build();
    }
    
    
    // 임시 리스트 화면 구현 시 기존 프로젝트 생성 페이지 Service 코드
    /*
    public void write(ProjectPublishingDTO projectPublishingDTO, MultipartFile imageFile, String selectedSkills
            , String projectDescription, BigDecimal projectBudget
            , LocalDate projectStartDate, LocalDate projectEndDate
            , LocalDateTime recruitDeadline, List<String> roles
            , List<String> categories, List<Integer> teamSizes, List<String> questions) {

        MemberEntity memberEntity = memberRepository.findById(projectPublishingDTO.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("회원 아이디가 없습니다."));

        projectPublishingDTO.setSelectedSkills(Arrays.asList(selectedSkills.split(",")));  // 콤마로 구분된 기술 리스트로 변환
        projectPublishingDTO.setProjectDescription(projectDescription);

        // 이미지 Base64 인코딩 처리
        String imageBase64 = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imageBase64 = convertToBase64(imageFile);  // Base64로 변환된 이미지 저장
        }

        // BoardEntity 생성 후 저장
        ProjectPublishingEntity projectPublishingEntity = ProjectPublishingEntity.builder()
                .clientId(memberEntity)
                .projectTitle(projectPublishingDTO.getProjectTitle())
                .recruitDeadline(recruitDeadline)
                .projectStartDate(projectStartDate)
                .projectEndDate(projectEndDate)
                .projectBudget(projectBudget)
                .projectDescription(projectDescription)
                .projectImage(imageBase64) // Base64로 변환된 이미지 저장
                .projectStatus(false)
                .build();
        projectPublishingRepository.save(projectPublishingEntity);

        // 관련 기술 저장 로직
        for (String skill : projectPublishingDTO.getSelectedSkills()) {
            ProjectRequiredSkillEntity skillEntity = ProjectRequiredSkillEntity.builder()
                    .projectPublishingEntity(projectPublishingEntity)
                    .skillText(skill)
                    .build();
            skillRepository.save(skillEntity);
        }

        // 모집 인원 저장 로직 (roles, categories, teamSizes 저장)
        for (int i = 0; i < roles.size(); i++) {
            WorkScopeEntity workScopeEntity = WorkScopeEntity.builder()
                    .projectPublishingEntity(projectPublishingEntity)
                    .workType(roles.get(i))
                    .requiredNum(teamSizes.get(i))
                    .build();
            workScopeRepository.save(workScopeEntity);
        }

        for (int i = 0; i < categories.size(); i++) {
            ProjectCategoryEntity categoryEntity = ProjectCategoryEntity.builder()
                    .projectPublishingEntity(projectPublishingEntity)
                    .category(categories.get(i))
                    .requiredNum(teamSizes.get(i))
                    .build();
            categoryRepository.save(categoryEntity);
        }

        // 사전 질문 저장 로직
        for (String question : questions) {
            PrequalificationQuestionEntity questionEntity = PrequalificationQuestionEntity.builder()
                    .projectPublishingEntity(projectPublishingEntity)
                    .questionText(question)
                    .build();
            prequalificationQuestionRepository.save(questionEntity);
        }
    }

    // Base64 변환 유틸리티 메서드
    private String convertToBase64(MultipartFile file) {
        try {
            byte[] fileContent = file.getBytes(); // MultipartFile을 바이트 배열로 변환
            return Base64.getEncoder().encodeToString(fileContent); // Base64로 인코딩
        } catch (IOException e) {
            throw new RuntimeException("파일을 Base64로 변환하는 데 실패했습니다.", e);
        }
    }
    
    public List<ProjectPublishingDTO> getList(String id) {
        
    	Sort sort = Sort.by(Sort.Direction.DESC, "projectNum");
    	List<ProjectPublishingEntity> entityList = projectPublishingRepository.findAll(sort);
    	
    	List<ProjectPublishingDTO> dtoList = new ArrayList<>();
    	
    	log.debug("현재 로그인한 Client ID 체크용: ", id);

        for (ProjectPublishingEntity entity : entityList) {
            if (entity.getClientId().getMemberId().equals(id)) {
            	ProjectPublishingDTO dto = ProjectPublishingDTO.builder()
                        .projectNum(entity.getProjectNum())
                        .clientId(entity.getClientId().getMemberId())
                        .projectTitle(entity.getProjectTitle())
                        .recruitDeadline(entity.getRecruitDeadline())
                        .projectStartDate(entity.getProjectStartDate())
                        .projectEndDate(entity.getProjectEndDate())
                        .projectBudget(entity.getProjectBudget())
                        .projectDescription(entity.getProjectDescription())
                        .projectImage(entity.getProjectImage())
                        .projectStatus(entity.getProjectStatus())
                        .build();
                dtoList.add(dto);
            }
        }

        return dtoList;
    }
    
    private ProjectPublishingDTO convertToDTO(ProjectPublishingEntity entity) {
        return ProjectPublishingDTO.builder()
                .projectNum(entity.getProjectNum())
                .clientId(entity.getClientId().getMemberId())
                .projectTitle(entity.getProjectTitle())
                .recruitDeadline(entity.getRecruitDeadline())
                .projectStartDate(entity.getProjectStartDate())
                .projectEndDate(entity.getProjectEndDate())
                .projectBudget(entity.getProjectBudget())
                .projectDescription(entity.getProjectDescription())
                .projectImage(entity.getProjectImage())  // 조회 시 Base64로 저장된 이미지 데이터
                .projectStatus(entity.getProjectStatus())
                .build();
    }
    
    public ProjectPublishingDTO getBoard(int pNum, String memberId, RoleName roleName) {
        ProjectPublishingEntity entity = projectPublishingRepository.findById(pNum)
                .orElseThrow(() -> new EntityNotFoundException("해당 번호의 글이 없습니다."));

        // DTO로 변환 작업
        ProjectPublishingDTO dto = convertToDTO(entity);

        LocalDate projectStartDate = entity.getProjectStartDate();
        LocalDate projectEndDate = entity.getProjectEndDate();
        long estimatedDays = ChronoUnit.DAYS.between(projectStartDate, projectEndDate);

        List<PrequalificationQuestionEntity> questions = prequalificationQuestionRepository.findByProjectPublishingEntity(entity);
        List<ProjectRequiredSkillEntity> skills = skillRepository.findByProjectPublishingEntity(entity);
        List<ProjectCategoryEntity> categories = categoryRepository.findByProjectPublishingEntity(entity);
        List<WorkScopeEntity> workScopes = workScopeRepository.findByProjectPublishingEntity(entity);

        List<String> selectedSkills = skills.stream()
                .map(ProjectRequiredSkillEntity::getSkillText)
                .collect(Collectors.toList());
        List<String> questionTexts = questions.stream()
                .map(PrequalificationQuestionEntity::getQuestionText)
                .collect(Collectors.toList());

        List<Map<String, Object>> matchedOutputs = new ArrayList<>();

        // 카테고리와 업무 범위를 매칭할 때, required_num이 같은 경우 매칭
        for (ProjectCategoryEntity category : categories) {
            for (WorkScopeEntity workScope : workScopes) {
                // required_num이 같은 경우 매칭
                if (category.getRequiredNum() == workScope.getRequiredNum()) {
                    Map<String, Object> output = new HashMap<>();
                    output.put("category", category.getCategory());
                    output.put("workScope", workScope.getWorkType());
                    output.put("requiredNum", category.getRequiredNum());  // 동일한 required_num
                    matchedOutputs.add(output);
                } else {
                    log.debug("매칭되지 않은 required_num: 카테고리 {}, 업무 범위 {}",
                            category.getRequiredNum(), workScope.getRequiredNum());
                }
            }
        }
        MemberEntity member = memberRepository.findByMemberIdAndRoleName(memberId, roleName);
        RoleName role = member.getRoleName();

        dto.setRoleName(role);
        dto.setEstimatedDay(estimatedDays);
        dto.setOutputList(matchedOutputs);
        dto.setSelectedSkills(selectedSkills);
        dto.setPrequalificationQuestions(questionTexts);

        return dto;
    }

    public void deleteBoard(int pNum) {
        projectPublishingRepository.deleteById(pNum);
    }
    */
    
    /*
	// ProjectNum을 기준으로 데이터를 가져오는 서비스 메서드
	public Map<String, Object> getGanttChartData(int projectNum) {
		// 주어진 프로젝트 번호를 기준으로 업무 데이터 조회
	    List<TaskManagementEntity> taskEntities = taskManagementRepository.findByProjectPublishingEntity_ProjectNum(projectNum);
	    
	    log.debug("taskEntities: {}", taskEntities);
	    log.debug("Task Entities Size: " + taskEntities.size());
	    
	    // TaskManagementEntity를 통해 FunctionTitleEntity를 조회하기 위한 함수 호출
	    Set<Integer> functionTitleIds = taskEntities.stream()
	        .map(task -> task.getFunctionTitleEntity().getFunctionTitleId())
	        .collect(Collectors.toSet());
	    
	    // FunctionTitleEntity를 조회 (ID 기반으로)
	    List<FunctionTitleEntity> functionEntities = functionTitleRepository.findAllById(functionTitleIds);

	    log.debug("functionEntities: {}", functionEntities);
	    
	    // FunctionTitle ID를 키로 사용하여 작업 리스트를 그룹화
	    Map<Integer, List<TaskManagementEntity>> functionTasksMap = taskEntities.stream()
	        .collect(Collectors.groupingBy(task -> task.getFunctionTitleEntity().getFunctionTitleId()));

        // 데이터 포맷 설정
        List<Map<String, Object>> functionsData = new ArrayList<>();
        AtomicInteger idCounter = new AtomicInteger(1);

        for (FunctionTitleEntity function : functionEntities) {
            List<Map<String, Object>> children = functionTasksMap.getOrDefault(function.getFunctionTitleId(), Collections.emptyList())
                .stream()
                .map(task -> {
                    Map<String, Object> taskData = new HashMap<>();
                    int taskId = idCounter.getAndIncrement();
                    taskData.put("id", taskId); // 간트차트 내의 ID
                    taskData.put("dbId", task.getTaskId()); // 실제 DB ID
                    taskData.put("entityType", "task");  // 엔티티 유형
                    taskData.put("name", task.getTaskTitle());
                    taskData.put("actualStart", task.getTaskStartDate() + "T00:00:00Z");
                    taskData.put("actualEnd", task.getTaskEndDate() + "T23:59:59Z");
                    taskData.put("progressValue", task.getTaskProcessivity());  // default 값 "0%"
                    taskData.put("baselineStart", task.getTaskStartDate() + "T00:00:00Z");
                    taskData.put("baselineEnd", task.getTaskEndDate() + "T23:59:59Z");
                    taskData.put("rowHeight", 35);
                    return taskData;
                }).collect(Collectors.toList());

            // Function Title의 baseline 날짜 계산
            LocalDate functionStartDate = functionTasksMap.getOrDefault(function.getFunctionTitleId(), Collections.emptyList())
                .stream().map(TaskManagementEntity::getTaskStartDate).min(LocalDate::compareTo).orElse(null);
            LocalDate functionEndDate = functionTasksMap.getOrDefault(function.getFunctionTitleId(), Collections.emptyList())
                .stream().map(TaskManagementEntity::getTaskEndDate).max(LocalDate::compareTo).orElse(null);

            Map<String, Object> functionData = new HashMap<>();
            functionData.put("id", idCounter.getAndIncrement()); // 간트차트 내의 ID
            functionData.put("dbId", function.getFunctionTitleId()); // 실제 DB ID
            functionData.put("entityType", "function"); // 엔티티 유형
            functionData.put("name", function.getTitleName());
            functionData.put("actualStart", functionStartDate != null ? functionStartDate + "T00:00:00Z" : null);
            functionData.put("actualEnd", functionEndDate != null ? functionEndDate + "T23:59:59Z" : null);
            functionData.put("progressValue", function.getFunctionProcessivity());  // default 값 "0%"
            functionData.put("baselineStart", functionStartDate != null ? functionStartDate + "T00:00:00Z" : null);
            functionData.put("baselineEnd", functionEndDate != null ? functionEndDate + "T23:59:59Z" : null);
            functionData.put("rowHeight", 35);
            functionData.put("children", children);

            System.out.println("Function Data: " + functionData);
            
            functionsData.add(functionData);
        }

        System.out.println("Functions Data: " + functionsData);
        
        // baselineStart를 기준으로 기능 데이터 정렬
        functionsData.sort((f1, f2) -> {
            String start1 = (String) f1.get("baselineStart");
            String start2 = (String) f2.get("baselineStart");
            return start1.compareTo(start2);
        });
        
        // 각 기능의 자식 업무를 baselineStart 기준으로 정렬
        for (Map<String, Object> functionData : functionsData) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> children = (List<Map<String, Object>>) functionData.get("children");
            children.sort((t1, t2) -> {
                String start1 = (String) t1.get("baselineStart");
                String start2 = (String) t2.get("baselineStart");
                return start1.compareTo(start2);
            });
        }

        // bufferDay 설정
        int bufferDay = 7;

        // 가장 처음 시작하는 업무의 시작일과 가장 마지막에 끝나는 업무의 종료일 계산
        LocalDate overallStartDate = functionsData.stream()
            .map(f -> (String) f.get("baselineStart"))
            .filter(Objects::nonNull)
            .map(start -> LocalDate.parse(start.substring(0, 10)))
            .min(LocalDate::compareTo)
            .orElse(null);

        LocalDate overallEndDate = functionsData.stream()
            .map(f -> (String) f.get("baselineEnd"))
            .filter(Objects::nonNull)
            .map(end -> LocalDate.parse(end.substring(0, 10)))
            .max(LocalDate::compareTo)
            .orElse(null);

        // 여유 기간 추가
        if (overallStartDate != null) {
            overallStartDate = overallStartDate.minusDays(bufferDay);
        }
        if (overallEndDate != null) {
            overallEndDate = overallEndDate.plusDays(bufferDay);
        }

        // 여유 기간을 적용한 시작일과 종료일 계산
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String adjustedStartDate = overallStartDate != null ? overallStartDate.atStartOfDay().format(formatter) : null;
        String adjustedEndDate = overallEndDate != null ? overallEndDate.atStartOfDay().plusDays(1).minusNanos(1).format(formatter) : null;

        Map<String, Object> response = new HashMap<>();
        response.put("data", functionsData);
        response.put("adjustedStartDate", adjustedStartDate);
        response.put("adjustedEndDate", adjustedEndDate);
        return response;
    }
    */
	
}
