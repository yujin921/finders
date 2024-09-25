package net.datasa.finders.service;

import lombok.RequiredArgsConstructor;
import net.datasa.finders.domain.dto.ClientDataDTO;
import net.datasa.finders.domain.dto.ClientReviewDTO;
import net.datasa.finders.domain.dto.ReviewItemDTO;
import net.datasa.finders.domain.entity.ClientReviewItemEntity;
import net.datasa.finders.domain.entity.ClientReviewsEntity;
import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.repository.ClientReviewsRepository;
import net.datasa.finders.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientReviewService {
    private final ClientReviewsRepository clientReviewRepository; // 클라이언트 리뷰 레포지토리
    private final MemberRepository memberRepository; // 멤버 레포지토리 추가

    // 특정 프로젝트에 참여 중인 모든 참가자 목록을 조회합니다.
    @Transactional(readOnly = true)
    public List<ClientDataDTO> getParticipantsByProject(int projectNum, String clientId) {
        List<MemberEntity> teamMembers = memberRepository.findByProjects_ProjectNum(projectNum);

        return teamMembers.stream()
                .filter(member -> !member.getMemberId().equals(clientId)) // 클라이언트 본인 제외
                .map(member -> {
                    boolean isReviewCompleted = clientReviewRepository.existsByProjectNumAndClientIdAndFreelancerId(
                            projectNum, clientId, member.getMemberId());
                    return new ClientDataDTO(member.getMemberId(), member.getMemberName(), isReviewCompleted);
                })
                .collect(Collectors.toList());
    }

    // 클라이언트가 평가한 리뷰 목록을 가져오는 메서드
    @Transactional(readOnly = true)
    public List<ClientReviewDTO> getReceivedReviews(String clientId) {
        // 프리랜서 ID를 사용하여 리뷰를 조회하고 DTO로 변환
        return clientReviewRepository.findByClientId(clientId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Entity를 DTO로 변환하는 메서드
    private ClientReviewDTO convertToDTO(ClientReviewsEntity entity) {
        return ClientReviewDTO.builder()
                .reviewId(entity.getReviewId())
                .projectNum(entity.getProjectNum())
                .clientId(entity.getClientId())
                .freelancerId(entity.getFreelancerId())
                .rating(entity.getRating())
                .comment(entity.getComment())
                .reviewDate(entity.getReviewDate())
                .reviewItems(entity.getReviewItems().stream()
                        .map(this::convertReviewItemToDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    // 리뷰 항목을 DTO로 변환하는 메서드
    private ReviewItemDTO convertReviewItemToDTO(ClientReviewItemEntity entity) {
        return ReviewItemDTO.builder()
                .itemName(entity.getItemName())
                .selected(entity.isItemValue()) // boolean으로 설정
                .build();
    }
    
    @Transactional
    public void createClientReview(ClientReviewDTO reviewDTO) {
    	  ClientReviewsEntity reviewEntity = ClientReviewsEntity.builder()
    	            .projectNum(reviewDTO.getProjectNum())
    	            .freelancerId(reviewDTO.getClientId()) // 평가를 남긴 ID를 received_id로 설정
    	            .clientId(reviewDTO.getFreelancerId()) // 평가를 받은 ID를 send_id로 설정
    	            .rating(reviewDTO.getRating())
    	            .comment(reviewDTO.getComment())
    	            .reviewDate(LocalDateTime.now())
    	            .build();
        // 리뷰 항목 변환 및 저장
        List<ClientReviewItemEntity> reviewItems = reviewDTO.getReviewItems().stream()
                .map(item -> {
                    ClientReviewItemEntity reviewItem = new ClientReviewItemEntity();
                    reviewItem.setItemName(item.getItemName());
                    reviewItem.setItemValue(item.isSelected());
                    reviewItem.setClientReview(reviewEntity); // 리뷰와 연관
                    return reviewItem;
                }).collect(Collectors.toList());

        reviewEntity.setReviewItems(reviewItems);

        // 저장
        clientReviewRepository.save(reviewEntity);
    }
    
    @Transactional(readOnly = true)
    public boolean isReviewCompleted(String freelancerId, int projectNum, String clientId) {
        
    	
    	return clientReviewRepository.existsByProjectNumAndClientIdAndFreelancerId(projectNum, freelancerId,clientId);
    }

    // 프로젝트 번호로 리뷰 평점 가져오기
    public Optional<Float> getAverageRatingForProject(int projectNum) {
        return clientReviewRepository.findAverageRatingByProjectNum(projectNum);
    }
}
