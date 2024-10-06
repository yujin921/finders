package net.datasa.finders.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.FindFreelancerDTO;
import net.datasa.finders.domain.dto.MemberDTO;
import net.datasa.finders.domain.dto.ProjectPublishingDTO;
import net.datasa.finders.domain.entity.FreelancerPortfoliosEntity;
import net.datasa.finders.domain.entity.FreelancerReviewsEntity;
import net.datasa.finders.domain.entity.FreelancerSkillEntity;
import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.domain.entity.ProjectPublishingEntity;
import net.datasa.finders.domain.entity.ProjectRequiredSkillEntity;
import net.datasa.finders.domain.entity.RoleName;
import net.datasa.finders.domain.entity.TeamEntity;
import net.datasa.finders.repository.FreelancerPortfoliosRepository;
import net.datasa.finders.repository.FreelancerReviewsRepository;
import net.datasa.finders.repository.FreelancerSkillRepository;
import net.datasa.finders.repository.MemberRepository;
import net.datasa.finders.repository.ProjectPublishingRepository;
import net.datasa.finders.repository.ProjectRequiredSkillRepository;
import net.datasa.finders.repository.TeamRepository;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class RecommendationService {

	// Repository 선언 - Spring에서 자동으로 주입됨
    private final MemberRepository memberRepository; 
    private final FreelancerReviewsRepository freelancerReviewsRepository; 
    private final FreelancerSkillRepository freelancerSkillsRepository; 
    private final ProjectPublishingRepository projectPublishingRepository; 
    private final ProjectRequiredSkillRepository projectRequiredSkillsRepository;
    private final FreelancerPortfoliosRepository freelancerPortfoliosRepository;
    private final TeamRepository teamRepository;
    
    public MemberDTO getCurrentUser(String userId) {
        // 사용자 ID로 MemberEntity를 조회
        MemberEntity member = memberRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // MemberDTO 생성
        MemberDTO memberDTO = new MemberDTO(
            member.getMemberId(),
            member.getMemberPw(),
            member.getMemberName(),
            member.getEmail(),
            null, // profileImg는 필요시 null로 설정
            member.isEnabled(),
            member.getRoleName(),
            member.getCreatedTime(),
            member.getUpdatedTime()
        );

        return memberDTO;
    }

    // 프리랜서에게 프로젝트를 추천해주기 위한 메소드
    public List<ProjectPublishingDTO> recommendProjects(String freelancerId) {
        // 1. 프리랜서의 평가 정보를 가져옴
        Map<String, Integer> targetFreelancerRatings = getFreelancerRatings(freelancerId);

        log.debug("targetFreelancerRatings 체크용!! : {}", targetFreelancerRatings);
        
        // 2. 모든 클라이언트 정보를 가져오고 MemberDTO로 변환
        List<MemberDTO> allClients = memberRepository.findAllByRoleName(RoleName.ROLE_CLIENT)
                .stream()
                .map(member -> new MemberDTO(
                        member.getMemberId(),
                        member.getMemberPw(),
                        member.getMemberName(),
                        member.getEmail(),
                        null, // 프로필 이미지 null 설정
                        member.isEnabled(),
                        member.getRoleName(),
                        member.getCreatedTime(),
                        member.getUpdatedTime()))
                .collect(Collectors.toList());

        log.debug("모든 클라이언트 정보: {}", allClients);
        
        // 3. 클라이언트와의 유사도 점수를 저장할 맵
        Map<String, Double> similarityScores = new HashMap<>();

        // 4. 각 클라이언트에 대해 유사도 점수 계산
        for (MemberDTO client : allClients) {
            // 5. 클라이언트의 평가 정보를 가져옴
            Map<String, Integer> clientRatings = getClientRatings(client.getMemberId());

            log.debug("clientRatings 체크용!! : {}", clientRatings);
            
            // 6. 평가 유사도 계산
            double ratingSimilarity = cosineSimilarity(targetFreelancerRatings, clientRatings);

            log.debug("ratingSimilarity 체크용!! : {}", ratingSimilarity);
            
            // 7. 클라이언트의 요구 기술 가져오기
            List<String> clientSkills = getClientSkills(client.getMemberId());

            log.debug("clientSkills 체크용!! : {}", clientSkills);
            
            // 8. 프리랜서의 기술 가져오기
            MemberEntity freelancer = memberRepository.findById(freelancerId)
                    .orElseThrow(() -> new RuntimeException("프리랜서를 찾을 수 없습니다."));

            log.debug("freelancer 체크용!! : {}", freelancer);
            
            List<FreelancerSkillEntity> skills = freelancerSkillsRepository.findByFreelancerId(freelancer);
            List<String> freelancerSkills = skills.stream()
                    .map(FreelancerSkillEntity::getSkillText)
                    .collect(Collectors.toList());

            log.debug("freelancerSkills 체크용!! : {}", freelancerSkills);
            
            // 9. 기술 유사도 계산
            double skillSimilarity = calculateSkillSimilarity(freelancerSkills, clientSkills);

            log.debug("skillSimilarity 체크용!! : {}", skillSimilarity);
            
            // 10. 최종 유사도 계산 (평가 유사도와 기술 유사도의 평균)
            double finalSimilarity = (ratingSimilarity + skillSimilarity) / 2;

            // 11. 유사도 점수를 저장
            similarityScores.put(client.getMemberId(), finalSimilarity);
            
            log.debug("finalSimilarity 체크용 : {}", finalSimilarity);
        }

        // 12. 추천 프로젝트를 가져오기
        Set<MemberEntity> clients = new HashSet<>();
        for (String clientId : similarityScores.keySet()) {
            MemberEntity client = memberRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("클라이언트를 찾을 수 없습니다."));
            clients.add(client);
        }
        List<ProjectPublishingEntity> allProjects = projectPublishingRepository.findAllByClientIdIn(clients);

        log.debug("모든 프로젝트 수: {}", allProjects.size());
        log.debug("allProjects 체크용 : {}", allProjects);
        
        // 13. 모집 중인 프로젝트와 모집 완료된 프로젝트 모두 조회
        List<ProjectPublishingEntity> ongoingProjects = allProjects.stream()
                .filter(project -> !project.getProjectStatus()) // 모집 중인 프로젝트만
                .sorted((p1, p2) -> {
                    // 유사도 점수 기반으로 정렬 (내림차순)
                    double score1 = similarityScores.getOrDefault(p1.getClientId(), 0.0);
                    double score2 = similarityScores.getOrDefault(p2.getClientId(), 0.0);
                    return Double.compare(score2, score1);
                })
                .collect(Collectors.toList());

        // 모집 완료된 프로젝트를 추가
        List<ProjectPublishingEntity> completedProjects = allProjects.stream()
                .filter(project -> project.getProjectStatus()) // 모집 완료된 프로젝트만
                .sorted((p1, p2) -> {
                    // 유사도 점수 기반으로 정렬 (내림차순)
                    double score1 = similarityScores.getOrDefault(p1.getClientId(), 0.0);
                    double score2 = similarityScores.getOrDefault(p2.getClientId(), 0.0);
                    return Double.compare(score2, score1);
                })
                .collect(Collectors.toList());

        // 두 리스트를 합칩니다. 모집 중인 프로젝트가 먼저 오도록
        ongoingProjects.addAll(completedProjects);
        
        log.debug("ongoingProjects 체크용 : {}", ongoingProjects);
        
        // 14. 추천 프로젝트 DTO 리스트로 변환하여 반환
        List<ProjectPublishingDTO> dtoList = new ArrayList<>();

        for (ProjectPublishingEntity project : ongoingProjects) {
            ProjectPublishingDTO dto = ProjectPublishingDTO.builder()
                    .projectNum(project.getProjectNum())
                    .clientId(project.getClientId().getMemberId()) // 클라이언트 ID
                    .projectTitle(project.getProjectTitle())
                    .recruitDeadline(project.getRecruitDeadline())
                    .projectStartDate(project.getProjectStartDate())
                    .projectEndDate(project.getProjectEndDate())
                    .projectBudget(project.getProjectBudget())
                    .projectDescription(project.getProjectDescription())
                    .projectImage(project.getProjectImage()) // Base64 변환
                    .projectStatus(project.getProjectStatus())
                    .projectCreateDate(project.getProjectCreateDate())
                    .build();
                
            dtoList.add(dto);
        }
        
        log.debug("dtoList 체크용 : {}", dtoList);

        return dtoList;
    }

    // 클라이언트가 프리랜서를 추천받기 위한 메소드
    public List<FindFreelancerDTO> recommendFreelancers(String clientId) {
        // 1. 클라이언트의 평가 정보를 가져옴
        Map<String, Integer> targetClientRatings = getClientRatings(clientId);
        log.debug("targetClientRatings 체크용!! : {}", targetClientRatings);
        
        // 2. 모든 프리랜서 정보를 가져옴
        List<MemberDTO> allFreelancers = memberRepository.findAllByRoleName(RoleName.ROLE_FREELANCER)
                .stream()
                .map(member -> new MemberDTO(
                        member.getMemberId(),
                        member.getMemberPw(),
                        member.getMemberName(),
                        member.getEmail(),
                        null, // 프로필 이미지 null 설정
                        member.isEnabled(),
                        member.getRoleName(),
                        member.getCreatedTime(),
                        member.getUpdatedTime()))
                .collect(Collectors.toList());

        log.debug("모든 프리랜서 정보: {}", allFreelancers);
        
        // 3. 프리랜서와의 유사도 점수를 저장할 맵
        Map<String, Double> similarityScores = new HashMap<>();

        // 4. 각 프리랜서에 대해 유사도 점수 계산
        for (MemberDTO freelancer : allFreelancers) {
            // 5. 프리랜서의 평가 정보를 가져옴
            Map<String, Integer> freelancerRatings = getFreelancerRatings(freelancer.getMemberId());
            log.debug("freelancerRatings 체크용!! : {}", freelancerRatings);
            
            // 6. 평가 유사도 계산
            double ratingSimilarity = cosineSimilarity(targetClientRatings, freelancerRatings);
            log.debug("ratingSimilarity 체크용!! : {}", ratingSimilarity);
            
            // 7. 클라이언트의 요구 기술 가져오기
            List<String> clientSkills = getClientSkills(clientId);
            log.debug("clientSkills 체크용!! : {}", clientSkills);
            
            // 8. 프리랜서의 기술 가져오기
            MemberEntity freelancerEntity = memberRepository.findById(freelancer.getMemberId())
                    .orElseThrow(() -> new RuntimeException("프리랜서를 찾을 수 없습니다.")); // 예외 처리
            
            List<FreelancerSkillEntity> skills = freelancerSkillsRepository.findByFreelancerId(freelancerEntity);
            List<String> freelancerSkills = skills.stream()
                    .map(FreelancerSkillEntity::getSkillText)
                    .collect(Collectors.toList());
            log.debug("freelancerSkills 체크용!! : {}", freelancerSkills);
            
            // 9. 기술 유사도 계산
            double skillSimilarity = calculateSkillSimilarity(clientSkills, freelancerSkills);
            log.debug("skillSimilarity 체크용!! : {}", skillSimilarity);
            
            // 10. 최종 유사도 계산 (평가 유사도와 기술 유사도의 평균)
            double finalSimilarity = (ratingSimilarity + skillSimilarity) / 2;
            log.debug("finalSimilarity 체크용 : {}", finalSimilarity);
            
            // 11. 유사도 점수를 저장
            similarityScores.put(freelancer.getMemberId(), finalSimilarity);
        }

        // 12. 추천 프리랜서를 가져오기
        List<FindFreelancerDTO> recommendedFreelancers = getRecommendedFreelancers(similarityScores);
        log.debug("추천 프리랜서 수: {}", recommendedFreelancers.size());
        
        return recommendedFreelancers;
    }
    
    // 추천 프리랜서 조회 함수
    public List<FindFreelancerDTO> getRecommendedFreelancers(Map<String, Double> similarityScores) {
        // 유사도 점수를 기준으로 프리랜서를 정렬합니다.
        List<Map.Entry<String, Double>> sortedFreelancers = new ArrayList<>(similarityScores.entrySet());
        sortedFreelancers.sort((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue()));

        List<FindFreelancerDTO> recommendedFreelancers = new ArrayList<>();

        // 모든 추천 프리랜서를 가져옵니다.
        for (Map.Entry<String, Double> entry : sortedFreelancers) {
            String freelancerId = entry.getKey();

            // 프리랜서의 세부 정보를 가져옵니다.
            MemberEntity freelancerEntity = memberRepository.findById(freelancerId)
                .orElseThrow(() -> new RuntimeException("프리랜서를 찾을 수 없습니다.")); // 예외 처리

            // 리뷰 정보를 가져옵니다.
            List<FreelancerReviewsEntity> freelancerReviews = freelancerReviewsRepository.findByReceivedId(freelancerId);
            
            double totalRating = freelancerReviews.stream()
                .mapToDouble(FreelancerReviewsEntity::getRating)
                .average()
                .orElse(0.0); // 평점이 없을 경우 0으로 설정

            // 프리랜서의 기술 정보를 가져옵니다.
            List<FreelancerSkillEntity> freelancerSkills = freelancerSkillsRepository.findByFreelancerId(freelancerEntity);
            String[] skills = freelancerSkills.stream()
                .map(FreelancerSkillEntity::getSkillText)
                .toArray(String[]::new);

            // 포트폴리오 수와 팀 정보를 가져옵니다.
            List<FreelancerPortfoliosEntity> freelancerPortfolios = freelancerPortfoliosRepository.findByMember(freelancerEntity);
            List<TeamEntity> teamEntities = teamRepository.findByMember(freelancerEntity);

            // FindFreelancerDTO 생성
            FindFreelancerDTO freelancerDTO = FindFreelancerDTO.builder()
                .memberId(freelancerEntity.getMemberId())
                .profileImg(freelancerEntity.getProfileImg())
                .totalRating(totalRating)
                .totalPortfolios(freelancerPortfolios.size())
                .totalReviews(freelancerReviews.size())
                .totalProjects(teamEntities.size())
                .skills(skills)
                .build();

            recommendedFreelancers.add(freelancerDTO);
        }

        return recommendedFreelancers; // 추천 프리랜서 리스트 반환
    }

    // 사용자 기반 필터링: 코사인 유사도 계산
    private double cosineSimilarity(Map<String, Integer> user1Ratings, Map<String, Integer> user2Ratings) {
        // 빈 맵 체크
        if (user1Ratings.isEmpty() || user2Ratings.isEmpty()) {
            return 0.0; // 둘 중 하나라도 비어있으면 유사도 0
        }
        
        double dotProduct = 0; // 두 벡터의 내적
        double normA = 0; // 첫 번째 벡터의 노름
        double normB = 0; // 두 번째 벡터의 노름

        // 1. user1의 평점을 기반으로 내적과 노름 계산
        for (String itemId : user1Ratings.keySet()) {
            if (user2Ratings.containsKey(itemId)) {
                dotProduct += user1Ratings.get(itemId) * user2Ratings.get(itemId);
            }
            normA += Math.pow(user1Ratings.get(itemId), 2);
        }
        
        // 2. user2의 평점을 기반으로 노름 계산
        for (Integer rating : user2Ratings.values()) {
            normB += Math.pow(rating, 2);
        }

        // 3. 코사인 유사도 계산 및 반환
        if (normA == 0 || normB == 0) {
            return 0.0; // 노름이 0일 경우 유사도 0
        }
        
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // 콘텐츠 기반 필터링: 스킬 유사도 계산
    private double calculateSkillSimilarity(List<String> freelancerSkills, List<String> clientSkills) {
        // 1. 클라이언트와 프리랜서의 모든 기술을 합침
        Set<String> allSkills = new HashSet<>(clientSkills);
        allSkills.addAll(freelancerSkills);
        double matches = 0; // 매칭된 기술 수

        // 디버깅: 합쳐진 모든 기술 목록의 크기 확인
        log.debug("모든 기술 목록: {}", allSkills);
        log.debug("기술 목록의 크기: {}", allSkills.size());
        
        // 2. 매칭된 기술 수를 계산
        for (String skill : allSkills) {
            if (clientSkills.contains(skill) && freelancerSkills.contains(skill)) {
                matches++;
            }
        }

        // 3. 기술의 비율을 계산하여 반환
        // 0으로 나누는 것을 방지하기 위한 조건 추가
        if (allSkills.size() == 0) {
            return 0; // 기술이 없을 경우 0 반환
        }
        
        // 디버깅: 매칭된 기술 수 및 결과 확인
        log.debug("매칭된 기술 수: {}", matches);
        log.debug("기술 비율 계산: matches = {}, allSkills.size() = {}", matches, allSkills.size());
        
        return matches / allSkills.size();
    }

    // 클라이언트의 평가 정보를 가져오는 메소드
    private Map<String, Integer> getClientRatings(String clientId) {
        List<FreelancerReviewsEntity> reviews = freelancerReviewsRepository.findBySendId(clientId);
        Map<String, Integer> ratings = new HashMap<>();

        // 각 리뷰에 대해 프리랜서 ID와 평점을 저장
        for (FreelancerReviewsEntity review : reviews) {
            ratings.put(review.getReceivedId(), Math.round(review.getRating()));
        }
        
        // 리뷰가 없을 경우 기본 점수 추가
        if (ratings.isEmpty()) {
            ratings.put("defaultFreelancerId", 0); // 기본 값 설정
        }
        
        return ratings;
    }

    // 프리랜서의 평가 정보를 가져오는 메소드
    private Map<String, Integer> getFreelancerRatings(String freelancerId) {
        List<FreelancerReviewsEntity> reviews = freelancerReviewsRepository.findByReceivedId(freelancerId); // 프리랜서의 리뷰 가져오기
        Map<String, Integer> ratings = new HashMap<>(); // 평점을 저장할 맵
        
        // 각 리뷰에 대해 클라이언트 ID와 평점을 저장
        for (FreelancerReviewsEntity review : reviews) {
            ratings.put(review.getSendId(), Math.round(review.getRating())); // 소수점을 반올림하여 저장
        }
        return ratings; // 평점 맵 반환
    }

    // 클라이언트의 기술 정보를 가져오는 메소드
    private List<String> getClientSkills(String clientId) {
    	// 클라이언트 ID로 MemberEntity를 조회
        MemberEntity client = memberRepository.findById(clientId)
            .orElseThrow(() -> new RuntimeException("클라이언트를 찾을 수 없습니다."));

        // 클라이언트의 프로젝트 가져오기
        List<ProjectPublishingEntity> projects = projectPublishingRepository.findByClientId(client); // MemberEntity를 사용
        
        List<String> skills = new ArrayList<>(); // 기술을 저장할 리스트
        
        // 각 프로젝트에 대해 요구 기술 정보를 가져옴
        for (ProjectPublishingEntity project : projects) {
        	// 프로젝트의 필수 기술 정보를 가져오기
        	List<ProjectRequiredSkillEntity> requiredSkills = projectRequiredSkillsRepository.findByProjectPublishingEntity(project);
            for (ProjectRequiredSkillEntity skill : requiredSkills) {
                skills.add(skill.getSkillText()); // 요구 기술 추가
            }
        }
        return skills; // 요구 기술 리스트 반환
    }
}
