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

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class ProjectManagementService {

	private final ProjectPublishingRepository projectPublishingRepository;
    private final MemberRepository memberRepository;
    private final WorkScopeRepository workScopeRepository;
    private final ProjectCategoryRepository categoryRepository;
    private final ProjectRequiredSkillRepository skillRepository;
    private final PrequalificationQuestionRepository prequalificationQuestionRepository;
    private final FunctionTitleRepository functionTitleRepository;
    private final TaskManagementRepository taskManagementRepository;
    private final ProjectManagementRepository projectManagementRepository;

    public List<ProjectPublishingDTO> getMyList(String id) {
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

        /*
        if (roleName.equals("ROLE_FREELANCER")) {
            // 프리랜서 ID로 프로젝트 목록 조회
            entityList = projectPublishingRepository.findByFreelancerId(id);
        } else {
            // 클라이언트와 관리자의 경우 모두 프로젝트 조회
            entityList = projectPublishingRepository.findAll(sort);
        }

        List<ProjectPublishingDTO> dtoList = new ArrayList<>();

        for (ProjectPublishingEntity entity : entityList) {
            // ROLE_ADMIN은 모든 프로젝트를 DTO로 추가
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

            if (roleName.equals("ROLE_CLIENT") && entity.getClientId().getMemberId().equals(id)) {
                // 클라이언트의 경우, 자신의 프로젝트만 추가
                dtoList.add(dto);
            } else if (roleName.equals("ROLE_FREELANCER")) {
                // 프리랜서의 경우, 해당 프로젝트 DTO 추가
                dtoList.add(dto);
            } else if (roleName.equals("ROLE_ADMIN")) {
                // 관리자는 모든 프로젝트 DTO 추가
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
        
        TaskStatus status = TaskStatus.valueOf(taskDTO.getTaskStatus().toUpperCase());
        TaskPriority priority = TaskPriority.valueOf(taskDTO.getTaskPriority().toUpperCase());
        LocalDate startDate = parseDate(taskDTO.getTaskStartDate());
        LocalDate endDate = parseDate(taskDTO.getTaskEndDate());

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
                .build();

        taskManagementRepository.save(taskManagementEntity);

        return taskDTO;
    }
    
    @Transactional
    public FunctionTitleDTO saveFunctionAndTask(int projectNum, String functionTitleName, TaskManagementDTO taskDTO) {
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
        saveTask(projectNum, taskDTO);

        return savedFunction;
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
    
    // 문자열을 LocalDate로 변환
    private LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + date, e);
        }
    }

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

                    // ProjectPublishingEntity가 존재하는 경우 projectNumber 가져오기
                    Integer projectNumber = (task.getProjectPublishingEntity() != null) ? task.getProjectPublishingEntity().getProjectNum() : null;

                    // TaskManagementDTO 객체 생성
                    return new TaskManagementDTO(
                        task.getTaskId(), // 업무 ID
                        projectNumber, // 프로젝트 번호
                        freelancerId, // 프리랜서 ID
                        functionTitleId, // 기능 제목 ID
                        functionTitleName, // 기능 제목 이름
                        task.getTaskTitle(), // 업무 제목
                        task.getTaskDescription(), // 업무 설명
                        task.getTaskStatus().name(), // 업무 상태 (Enum을 문자열로 변환)
                        task.getTaskPriority().name(), // 업무 우선순위 (Enum을 문자열로 변환)
                        task.getTaskStartDate().toString(), // 업무 시작 날짜 (LocalDate를 문자열로 변환)
                        task.getTaskEndDate().toString(), // 업무 종료 날짜 (LocalDate를 문자열로 변환)
                        (task.getActualStartDate() != null) ? task.getActualStartDate().toString() : null, // 실제 시작 날짜
                        (task.getActualEndDate() != null) ? task.getActualEndDate().toString() : null, // 실제 종료 날짜
                        task.getTaskProcessivity() // 업무 진행도
                    );
                })
                .collect(Collectors.toList()); // 변환된 DTO 리스트를 반환
    }

	public void projectCompletion(int projectNum) {
		TaskDateRangeDTO taskDateRange = taskManagementRepository.findTaskDateRangeByProjectNum(projectNum)
                .orElseThrow(() -> new EntityNotFoundException("업무가 존재하지 않아 일정 조회가 불가합니다."));
		
		ProjectPublishingEntity project = projectPublishingRepository.findById(projectNum)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 프로젝트입니다."));
		
		if (taskDateRange.getLatestEndDate().isAfter(project.getProjectEndDate())) {
			// 지연 기간 계산
            Period period = Period.between(project.getProjectEndDate(), taskDateRange.getLatestEndDate());
            int delayedDays = period.getDays() + (period.getMonths() * 30) + (period.getYears() * 365);
			
			ProjectManagementEntity projectManagementEntity = ProjectManagementEntity.builder()
					.projectPublishing(project)
					.actualStartDate(taskDateRange.getEarliestStartDate())
					.actualEndDate(taskDateRange.getLatestEndDate())
					.delayedStatus(true)
					.delayedDate(delayedDays)
					.build();
			
			projectManagementRepository.save(projectManagementEntity);
		} else {
			ProjectManagementEntity projectManagementEntity = ProjectManagementEntity.builder()
					.projectPublishing(project)
					.actualStartDate(taskDateRange.getEarliestStartDate())
					.actualEndDate(taskDateRange.getLatestEndDate())
					.delayedStatus(false)
					.delayedDate(0)
					.build();
			
			projectManagementRepository.save(projectManagementEntity);
		}
		
	}

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
	                    
	                    /*
	                    // 시작일과 종료일 값을 설정할 변수
	                    LocalDate startDate = (task.getActualStartDate() != null) ? task.getActualStartDate() : task.getTaskStartDate();
	                    LocalDate endDate = (task.getActualEndDate() != null) ? task.getActualEndDate() : task.getTaskEndDate();

	                    // taskData에 값을 추가
	                    taskData.put("actualStart", (startDate != null ? startDate.toString() : "") + "T00:00:00Z");
	                    taskData.put("actualEnd", (endDate != null ? endDate.toString() : "") + "T23:59:59Z");
	                    
	                    taskData.put("progressValue", task.getTaskProcessivity());  // default 값 "0%"
	                    taskData.put("baselineStart", task.getTaskStartDate() + "T00:00:00Z");
	                    taskData.put("baselineEnd", task.getTaskEndDate() + "T23:59:59Z");
	                    */
	                    
	                    // taskData에 값을 추가
	                    LocalDate startDate = task.getActualStartDate() != null ? task.getActualStartDate() : task.getTaskStartDate();
	                    LocalDate endDate = task.getActualEndDate() != null ? task.getActualEndDate() : task.getTaskEndDate();

	                    // actual을 baseline과 동일하게 설정(actualStart와 actualEnd가 null인 경우 기본 날짜로 설정)
	                    taskData.put("actualStart", (startDate != null ? startDate + "T00:00:00Z" : task.getTaskStartDate() + "T00:00:00Z"));
	                    taskData.put("actualEnd", (endDate != null ? endDate + "T23:59:59Z" : task.getTaskEndDate() + "T23:59:59Z"));

	                    // taskData에 값을 추가
	                    taskData.put("baselineStart", task.getTaskStartDate() + "T00:00:00Z");
	                    taskData.put("baselineEnd", task.getTaskEndDate() + "T23:59:59Z");
	                    taskData.put("progressValue", task.getTaskProcessivity());  // default 값 "0%"
	                    taskData.put("rowHeight", 35);
	                    return taskData;
	        }).collect(Collectors.toList());

	        // Function Title의 baseline 날짜 계산
	        LocalDate functionStartDate = functionTasksMap.getOrDefault(function.getFunctionTitleId(), Collections.emptyList())
	                .stream().map(TaskManagementEntity::getTaskStartDate).min(LocalDate::compareTo).orElse(null);
	        LocalDate functionEndDate = functionTasksMap.getOrDefault(function.getFunctionTitleId(), Collections.emptyList())
	                .stream().map(TaskManagementEntity::getTaskEndDate).max(LocalDate::compareTo).orElse(null);

	        // Function Title의 actual 날짜 계산
	        LocalDate functionActualStart = functionTasksMap.getOrDefault(function.getFunctionTitleId(), Collections.emptyList())
	                .stream()
	                .map(TaskManagementEntity::getActualStartDate)
	                .filter(Objects::nonNull)
	                .min(LocalDate::compareTo)
	                .orElse(null);
	        LocalDate functionActualEnd = functionTasksMap.getOrDefault(function.getFunctionTitleId(), Collections.emptyList())
	                .stream()
	                .map(TaskManagementEntity::getActualEndDate)
	                .filter(Objects::nonNull)
	                .max(LocalDate::compareTo)
	                .orElse(null);
	            
	        Map<String, Object> functionData = new HashMap<>();
	        functionData.put("id", idCounter.getAndIncrement()); // 간트차트 내의 ID
	        functionData.put("dbId", function.getFunctionTitleId()); // 실제 DB ID
	        functionData.put("entityType", "function"); // 엔티티 유형
	        functionData.put("name", function.getTitleName());
	        functionData.put("actualStart", functionActualStart != null ? functionActualStart + "T00:00:00Z" : functionStartDate + "T00:00:00Z");
	        functionData.put("actualEnd", functionActualEnd != null ? functionActualEnd + "T23:59:59Z" : functionEndDate + "T23:59:59Z");
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
	    int bufferDay = 30;

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

    // 실제 일정 업데이트
    public void updateSchedule(String entityType, int dbId, String actualStartDateStr, String actualEndDateStr) {
        log.debug("entityType check용: {}", entityType);
        log.debug("dbId check용: {}", dbId);
        log.debug("actualStartDateStr check용: {}", actualStartDateStr);
        log.debug("actualEndDateStr check용: {}", actualEndDateStr);

        if (!"task".equalsIgnoreCase(entityType)) {
            throw new IllegalArgumentException("유효하지 않은 Entity 유형입니다. EntityType: " + entityType);
        }

        // 문자열을 LocalDate로 변환
        LocalDate actualStart = parseDate(actualStartDateStr);
        LocalDate actualEnd = parseDate(actualEndDateStr);

        // TaskManagementEntity 처리
        TaskManagementEntity task = taskManagementRepository.findById(dbId)
            .orElseThrow(() -> new RuntimeException("작업을 찾을 수 없습니다. ID: " + dbId));

        // 실제 시작일 및 종료일 업데이트
        task.setActualStartDate(actualStart);
        task.setActualEndDate(actualEnd);

        // 작업 저장
        taskManagementRepository.save(task);
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
	
}
