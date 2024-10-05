package net.datasa.finders.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.ProjectPublishingDTO;
import net.datasa.finders.domain.entity.*;
import net.datasa.finders.repository.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 게시판 서비스
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class ProjectPublishingService {
    private final ProjectPublishingRepository projectPublishingRepository;
    private final MemberRepository memberRepository;
    private final WorkScopeRepository workScopeRepository;
    private final ProjectCategoryRepository categoryRepository;
    private final ProjectRequiredSkillRepository skillRepository;
    private final PrequalificationQuestionRepository prequalificationQuestionRepository;
    private final ClientReviewsRepository clientReviewsRepository;
    private final ClientReviewItemRepository clientReviewItemRepository;
    private final ProjectRequiredSkillRepository projectRequiredSkillRepository;
    private final ProjectCategoryRepository projectCategoryRepository;

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
                .projectCreateDate(LocalDateTime.now())
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
   
    public List<ProjectPublishingDTO> getList(String word) {
        
       Sort sort = Sort.by(Sort.Direction.DESC, "projectNum");
       List<ProjectPublishingEntity> entityList = projectPublishingRepository.findByProjectTitleContainingOrProjectDescriptionContaining(word, word);
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
                    .projectCreateDate(entity.getProjectCreateDate())
                    .build();
            dtoList.add(dto);
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
                .projectCreateDate(entity.getProjectCreateDate())
                .build();
    }
    
    public ProjectPublishingDTO getBoard(int pNum, String memberId, RoleName roleName) {
        ProjectPublishingEntity entity = projectPublishingRepository.findById(pNum)
                .orElseThrow(() -> new EntityNotFoundException("해당 번호의 글이 없습니다."));

        // DTO로 변환 작업
        ProjectPublishingDTO dto = convertToDTO(entity);

        LocalDate projectStartDate = entity.getProjectStartDate();
        LocalDate projectEndDate = entity.getProjectEndDate();
        LocalDateTime projectCreateDate = entity.getProjectCreateDate();
        String projectDescription = entity.getProjectDescription();
        
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
        Set<String> seenPairs = new HashSet<>();

        // 카테고리와 업무 범위를 매칭할 때, required_num이 같은 경우 매칭
        for (ProjectCategoryEntity category : categories) {
            for (WorkScopeEntity workScope : workScopes) {
                // required_num이 같은 경우 매칭
                if (category.getRequiredNum() == workScope.getRequiredNum()) {
                    String uniqueKey = category.getCategory() + ":" + workScope.getWorkType();
                    if (!seenPairs.contains(uniqueKey)) {
                        Map<String, Object> output = new HashMap<>();
                        output.put("category", category.getCategory());
                        output.put("workScope", workScope.getWorkType());
                        output.put("requiredNum", category.getRequiredNum());  // 동일한 required_num
                        matchedOutputs.add(output);
                        seenPairs.add(uniqueKey);  // 중복 방지용 키 추가
                    } else {
                        log.debug("중복된 항목: 카테고리 {}, 업무 범위 {}",
                                category.getCategory(), workScope.getWorkType());
                    }
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
        dto.setProjectCreateDate(projectCreateDate);
        dto.setProjectDescription(projectDescription);

        return dto;
    }

    public void deleteBoard(int pNum) {
        projectPublishingRepository.deleteById(pNum);
    }

    public ProjectPublishingDTO getBoardByProjectNum(int projectNum) {
        ProjectPublishingEntity projectEntity = projectPublishingRepository.findById(projectNum)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project number"));

        List<WorkScopeEntity> workScopes = workScopeRepository.findByProjectPublishingEntity(projectEntity);
        List<ProjectRequiredSkillEntity> skills = projectRequiredSkillRepository.findByProjectPublishingEntity(projectEntity);
        List<ProjectCategoryEntity> categories = projectCategoryRepository.findByProjectPublishingEntity(projectEntity);

        // DTO에 값 채우기
        return ProjectPublishingDTO.builder()
                .projectNum(projectEntity.getProjectNum())
                .clientId(projectEntity.getClientId().getMemberId())
                .projectTitle(projectEntity.getProjectTitle())
                .recruitDeadline(projectEntity.getRecruitDeadline())
                .projectStartDate(projectEntity.getProjectStartDate())
                .projectEndDate(projectEntity.getProjectEndDate())
                .projectBudget(projectEntity.getProjectBudget())
                .projectDescription(projectEntity.getProjectDescription())
                .selectedSkills(skills.stream().map(ProjectRequiredSkillEntity::getSkillText).collect(Collectors.toList()))
                .selectedCategories(categories.stream().map(ProjectCategoryEntity::getCategory).collect(Collectors.toList()))
                .selectedWorkScopes(workScopes.stream().map(WorkScopeEntity::getWorkType).collect(Collectors.toList()))
                .build();
    }

    public void updateBoard(ProjectPublishingDTO projectPublishingDTO,
                            MultipartFile projectImageFile,
                            String selectedSkills,
                            String projectDescription,
                            BigDecimal projectBudget,
                            LocalDate projectStartDate,
                            LocalDate projectEndDate,
                            LocalDateTime recruitDeadline,
                            List<String> roles,
                            List<String> categories,
                            List<Integer> teamSizes,
                            List<String> questions) {
        // 기존 프로젝트 엔티티 가져오기
        ProjectPublishingEntity projectPublishingEntity = projectPublishingRepository.findById(projectPublishingDTO.getProjectNum())
                .orElseThrow(() -> new EntityNotFoundException("프로젝트를 찾을 수 없습니다."));


        // 선택한 기술 리스트 업데이트
        projectPublishingDTO.setSelectedSkills(Arrays.asList(selectedSkills.split(",")));

        // 이미지 Base64 인코딩 처리
        String imageBase64 = null;
        if (projectImageFile != null && !projectImageFile.isEmpty()) {
            imageBase64 = convertToBase64(projectImageFile);  // Base64로 변환된 이미지
            projectPublishingEntity.setProjectImage(imageBase64); // 이미지 업데이트
        }

        // 기존 프로젝트 엔티티 업데이트
        projectPublishingEntity.setProjectTitle(projectPublishingDTO.getProjectTitle());
        projectPublishingEntity.setRecruitDeadline(recruitDeadline);
        projectPublishingEntity.setProjectStartDate(projectStartDate);
        projectPublishingEntity.setProjectEndDate(projectEndDate);
        projectPublishingEntity.setProjectBudget(projectBudget);
        projectPublishingEntity.setProjectDescription(projectDescription);

        // 프로젝트 엔티티 저장 (변경된 내용 반영)
        projectPublishingRepository.save(projectPublishingEntity);

        // 관련 기술 업데이트 로직 (기존 기술 수정)
        List<ProjectRequiredSkillEntity> existingSkills = skillRepository.findByProjectPublishingEntity(projectPublishingEntity);
        updateOrSaveSkills(existingSkills, projectPublishingDTO.getSelectedSkills(), projectPublishingEntity);

        // 모집 인원 업데이트 로직 (roles, categories, teamSizes 수정)
        List<WorkScopeEntity> existingWorkScopes = workScopeRepository.findByProjectPublishingEntity(projectPublishingEntity);
        updateOrSaveWorkScopes(existingWorkScopes, roles, teamSizes, projectPublishingEntity);

        List<ProjectCategoryEntity> existingCategories = categoryRepository.findByProjectPublishingEntity(projectPublishingEntity);
        updateOrSaveCategories(existingCategories, categories, teamSizes, projectPublishingEntity);

        // 사전 질문 업데이트 로직 (기존 질문 수정)
        List<PrequalificationQuestionEntity> existingQuestions = prequalificationQuestionRepository.findByProjectPublishingEntity(projectPublishingEntity);
        updateOrSaveQuestions(existingQuestions, questions, projectPublishingEntity);
    }

    private void updateOrSaveSkills(List<ProjectRequiredSkillEntity> existingSkills, List<String> newSkills, ProjectPublishingEntity projectPublishingEntity) {
        for (int i = 0; i < newSkills.size(); i++) {
            if (i < existingSkills.size()) {
                existingSkills.get(i).setSkillText(newSkills.get(i)); // 기존 기술 수정
            } else {
                // 새로운 기술 추가
                ProjectRequiredSkillEntity skillEntity = ProjectRequiredSkillEntity.builder()
                        .projectPublishingEntity(projectPublishingEntity)
                        .skillText(newSkills.get(i))
                        .build();
                skillRepository.save(skillEntity);
            }
        }
        // 기존 리스트가 더 길면 남은 기존 항목 삭제
        if (existingSkills.size() > newSkills.size()) {
            for (int i = newSkills.size(); i < existingSkills.size(); i++) {
                skillRepository.delete(existingSkills.get(i));
            }
        }
    }

    private void updateOrSaveWorkScopes(List<WorkScopeEntity> existingWorkScopes, List<String> roles, List<Integer> teamSizes, ProjectPublishingEntity projectPublishingEntity) {
        for (int i = 0; i < roles.size(); i++) {
            if (i < existingWorkScopes.size()) {
                existingWorkScopes.get(i).setWorkType(roles.get(i));
                existingWorkScopes.get(i).setRequiredNum(teamSizes.get(i));
            } else {
                WorkScopeEntity workScopeEntity = WorkScopeEntity.builder()
                        .projectPublishingEntity(projectPublishingEntity)
                        .workType(roles.get(i))
                        .requiredNum(teamSizes.get(i))
                        .build();
                workScopeRepository.save(workScopeEntity);
            }
        }
        // 기존 리스트가 더 길면 남은 기존 항목 삭제
        if (existingWorkScopes.size() > roles.size()) {
            for (int i = roles.size(); i < existingWorkScopes.size(); i++) {
                workScopeRepository.delete(existingWorkScopes.get(i));
            }
        }
    }

    private void updateOrSaveCategories(List<ProjectCategoryEntity> existingCategories, List<String> categories, List<Integer> teamSizes, ProjectPublishingEntity projectPublishingEntity) {
        for (int i = 0; i < categories.size(); i++) {
            if (i < existingCategories.size()) {
                existingCategories.get(i).setCategory(categories.get(i)); // 기존 카테고리 수정
                existingCategories.get(i).setRequiredNum(teamSizes.get(i)); // 팀 사이즈 수정
            } else {
                // 새로운 카테고리 추가
                ProjectCategoryEntity categoryEntity = ProjectCategoryEntity.builder()
                        .projectPublishingEntity(projectPublishingEntity)
                        .category(categories.get(i))
                        .requiredNum(teamSizes.get(i))
                        .build();
                categoryRepository.save(categoryEntity);
            }
        }
        // 기존 리스트가 더 길면 남은 기존 항목 삭제
        if (existingCategories.size() > categories.size()) {
            for (int i = categories.size(); i < existingCategories.size(); i++) {
                categoryRepository.delete(existingCategories.get(i));
            }
        }
    }

    private void updateOrSaveQuestions(List<PrequalificationQuestionEntity> existingQuestions, List<String> newQuestions, ProjectPublishingEntity projectPublishingEntity) {
        for (int i = 0; i < newQuestions.size(); i++) {
            if (i < existingQuestions.size()) {
                existingQuestions.get(i).setQuestionText(newQuestions.get(i)); // 기존 질문 수정
            } else {
                // 새로운 질문 추가
                PrequalificationQuestionEntity questionEntity = PrequalificationQuestionEntity.builder()
                        .projectPublishingEntity(projectPublishingEntity)
                        .questionText(newQuestions.get(i))
                        .build();
                prequalificationQuestionRepository.save(questionEntity);
            }
        }
        // 기존 리스트가 더 길면 남은 기존 항목 삭제
        if (existingQuestions.size() > newQuestions.size()) {
            for (int i = newQuestions.size(); i < existingQuestions.size(); i++) {
                prequalificationQuestionRepository.delete(existingQuestions.get(i));
            }
        }
    }

//    public List<ClientReviewsEntity> getClientReviews(String clientId) {
//        return clientReviewsRepository.findByClientId(clientId);
//    }

    /*
    public List<ClientReviewsEntity> getClientReviews(String clientId) {
        List<ClientReviewsEntity> reviews = clientReviewsRepository.findByClientId(clientId);
        log.debug("Found {} reviews for client {}", reviews.size(), clientId);
        return reviews;
    }*/

    public List<ProjectPublishingDTO> getLatestProjects(int limit) {
        Sort sort = Sort.by(Sort.Direction.DESC, "projectCreateDate");
        List<ProjectPublishingEntity> entityList = projectPublishingRepository.findAll(sort);
        List<ProjectPublishingDTO> dtoList = new ArrayList<>();

        for (int i = 0; i < Math.min(limit, entityList.size()); i++) {
            ProjectPublishingEntity entity = entityList.get(i);
            ProjectPublishingDTO dto = convertToDTO(entity);
            dtoList.add(dto);
        }

        return dtoList;
    }
}