//package net.datasa.finders.service;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import net.datasa.finders.repository.ClientReviewsRepository;
//import net.datasa.finders.repository.FreelancerSkillRepository;
//import net.datasa.finders.repository.ProjectRepository;
//
////채팅전용
//@Service
//public class RecommendationService {
//
//    @Autowired
//    private ProjectRepository projectRepository;
//    @Autowired
//    private FreelancerSkillRepository freelancerSkillRepository;
//    @Autowired
//    private ClientReviewsRepository reviewRepository;
//
//    public List<Project> getRecommendedProjectsForFreelancer(String freelancerId) {
//        // 프리랜서 보유 기술 가져오기
//        List<String> freelancerSkills = freelancerSkillRepository.findSkillsByFreelancerId(freelancerId);
//        
//        // 모든 프로젝트 가져오기
//        List<Project> allProjects = projectRepository.findAll();
//        
//        // 프로젝트 추천 점수 계산
//        List<ProjectRecommendation> recommendations = new ArrayList<>();
//        for (Project project : allProjects) {
//            // 프로젝트 요구 기술 가져오기
//            List<String> requiredSkills = project.getRequiredSkills();
//            
//            // 기술 매칭 점수 계산
//            int skillMatchCount = (int) requiredSkills.stream()
//                                    .filter(freelancerSkills::contains)
//                                    .count();
//                                    
//            // 리뷰 점수 가져오기 (평점 또는 클라이언트 평판)
//            double reviewScore = reviewRepository.getAverageRatingForProject(project.getProjectNum());
//            
//            // 최종 추천 점수 계산 (여기서 가중치 조정 가능)
//            double recommendationScore = calculateRecommendationScore(skillMatchCount, reviewScore);
//            
//            recommendations.add(new ProjectRecommendation(project, recommendationScore));
//        }
//        
//        // 추천 점수에 따라 정렬
//        recommendations.sort((r1, r2) -> Double.compare(r2.getRecommendationScore(), r1.getRecommendationScore()));
//        
//        // 추천된 프로젝트 리스트 반환
//        return recommendations.stream()
//                .map(ProjectRecommendation::getProject)
//                .collect(Collectors.toList());
//    }
//
//    // 추천 점수 계산 로직 (가중치 적용)
//    private double calculateRecommendationScore(int skillMatchCount, double reviewScore) {
//        double skillWeight = 0.7;
//        double reviewWeight = 0.3;
//        return (skillMatchCount * skillWeight) + (reviewScore * reviewWeight);
//    }
//
//
//
//
//    
//}
