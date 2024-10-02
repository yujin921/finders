package net.datasa.finders.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import net.datasa.finders.domain.dto.ClientReviewDTO;
import net.datasa.finders.domain.entity.ClientReviewItemEntity;
import net.datasa.finders.domain.entity.ClientReviewsEntity;
import net.datasa.finders.repository.ClientReviewItemRepository;
import net.datasa.finders.repository.ClientReviewsRepository;

@Service
@RequiredArgsConstructor
public class ClientReviewService {

    private final ClientReviewsRepository clientReviewsRepository;
    private final ClientReviewItemRepository clientReviewItemRepository;

    @Transactional
    public void saveClientReview(ClientReviewDTO clientreviewDTO) {
        
        // 리뷰가 이미 작성되었는지 확인: 프로젝트 번호, 보내는 사람, 받는 사람 기준
        boolean reviewExists = clientReviewsRepository.existsByProjectNumAndSendIdAndReceivedId(
                clientreviewDTO.getProjectNum(), 
                clientreviewDTO.getSendId(),
                clientreviewDTO.getParticipantId()
        );

        if (reviewExists) {
            throw new IllegalStateException("해당 프로젝트에서 이 사용자에게 이미 리뷰가 작성되었습니다.");
        }

        // 클라이언트 리뷰 저장
        ClientReviewsEntity reviewEntity = ClientReviewsEntity.builder()
            .projectNum(clientreviewDTO.getProjectNum())
            .sendId(clientreviewDTO.getSendId())
            .receivedId(clientreviewDTO.getParticipantId())
            .rating(clientreviewDTO.getRating())
            .comment(clientreviewDTO.getComment())
            .reviewDate(LocalDateTime.now())
            .build();

        clientReviewsRepository.save(reviewEntity);

        // 체크박스 평가 항목 저장
        List<ClientReviewItemEntity> reviewItems = clientreviewDTO.getReviewItems().stream()
            .map(item -> ClientReviewItemEntity.builder()
                .clientReview(reviewEntity)  // clientReview 필드에 리뷰 엔터티 연결
                .itemName(item.getItemName())  // ReviewItemDTO에서 itemName 가져오기
                .itemValue(item.isSelected())  // ReviewItemDTO에서 selected 가져오기
                .build())
            .collect(Collectors.toList());

        clientReviewItemRepository.saveAll(reviewItems);
    }
    
    public List<ClientReviewsEntity> getClientReviewsByClientId(String clientId) {
        // 클라이언트 ID를 기준으로 해당 클라이언트에 대한 모든 프로젝트 리뷰를 조회
        return clientReviewsRepository.findByReceivedId(clientId);
    }
}
