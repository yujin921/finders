package net.datasa.finders.service;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.FunctionTitleDTO;
import net.datasa.finders.domain.dto.ProjectPublishingDTO;
import net.datasa.finders.domain.dto.TaskDateRangeDTO;
import net.datasa.finders.domain.dto.TaskManagementDTO;
import net.datasa.finders.domain.entity.FunctionTitleEntity;
import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.domain.entity.PrequalificationQuestionEntity;
import net.datasa.finders.domain.entity.ProjectCategoryEntity;
import net.datasa.finders.domain.entity.ProjectManagementEntity;
import net.datasa.finders.domain.entity.ProjectPublishingEntity;
import net.datasa.finders.domain.entity.ProjectRequiredSkillEntity;
import net.datasa.finders.domain.entity.RoleName;
import net.datasa.finders.domain.entity.TaskManagementEntity;
import net.datasa.finders.domain.entity.TaskPriority;
import net.datasa.finders.domain.entity.TaskStatus;
import net.datasa.finders.domain.entity.WorkScopeEntity;
import net.datasa.finders.repository.FunctionTitleRepository;
import net.datasa.finders.repository.MemberRepository;
import net.datasa.finders.repository.PrequalificationQuestionRepository;
import net.datasa.finders.repository.ProjectCategoryRepository;
import net.datasa.finders.repository.ProjectManagementRepository;
import net.datasa.finders.repository.ProjectPublishingRepository;
import net.datasa.finders.repository.ProjectRequiredSkillRepository;
import net.datasa.finders.repository.TaskManagementRepository;
import net.datasa.finders.repository.TeamRepository;
import net.datasa.finders.repository.WorkScopeRepository;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class ProjectManagementService {

	private final ProjectPublishingRepository projectPublishingRepository;
    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;
    private final WorkScopeRepository workScopeRepository;
    private final ProjectCategoryRepository categoryRepository;
    private final ProjectRequiredSkillRepository skillRepository;
    private final PrequalificationQuestionRepository prequalificationQuestionRepository;
    private final FunctionTitleRepository functionTitleRepository;
    private final TaskManagementRepository taskManagementRepository;
    private final ProjectManagementRepository projectManagementRepository;

    public List<ProjectPublishingDTO> getMyList(String id) {
        
    	Sort sort = Sort.by(Sort.Direction.DESC, "projectNum");
    	List<ProjectPublishingEntity> entityList = projectPublishingRepository.findAll(sort);
    	
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
    
    public FunctionTitleDTO saveFunction(String functionTitleName) {
    	FunctionTitleEntity functionTitleEntity = FunctionTitleEntity.builder()
                .titleName(functionTitleName)
                .build();
        
        functionTitleRepository.save(functionTitleEntity);
        
        return new FunctionTitleDTO(functionTitleEntity.getFunctionTitleId(), functionTitleEntity.getTitleName());
	}
    
    public List<FunctionTitleDTO> getAllFunctionTitles() {
        List<FunctionTitleEntity> entities = functionTitleRepository.findAll();
        return entities.stream()
                .map(entity -> new FunctionTitleDTO(entity.getFunctionTitleId(), entity.getTitleName()))
                .collect(Collectors.toList());
    }
    
    private LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + date, e);
        }
    }

	public TaskManagementDTO saveTask(int projectNum, TaskManagementDTO taskDTO) {
		
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
		
		TaskManagementEntity taskManagementEntity = TaskManagementEntity.builder()
				.projectPublishingEntity(projectPublishing)
				.memberEntity(member)
				.functionTitleEntity(functionTitle)
				.taskTitle(taskDTO.getTaskTitle())
				.taskDescription(taskDTO.getTaskDescription())
				.taskStatus(status)
				.taskPriority(priority)
				.taskStartDate(startDate) // 변환된 LocalDate 사용
				.taskEndDate(endDate)     // 변환된 LocalDate 사용
				.build();

        taskManagementRepository.save(taskManagementEntity);
		
        return taskDTO;
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
                        task.getTaskEndDate().toString() // 업무 종료 날짜 (LocalDate를 문자열로 변환)
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
