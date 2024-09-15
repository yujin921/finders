package net.datasa.finders.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FreelancerReviewItemDTO {
    private int itemId;
    private String itemName;
    private boolean itemValue; // `boolean` 타입과 맞추기
}
