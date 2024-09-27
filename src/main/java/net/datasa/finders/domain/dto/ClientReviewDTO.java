package net.datasa.finders.domain.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientReviewDTO {
    private int projectNum;
    private String sendId; // 평가를 남긴 사용자
    private String participantId; // 평가받는 사용자
    private float rating;
    private String comment;
    private List<ReviewItemDTO> reviewItems;}
