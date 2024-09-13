package net.datasa.finders.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor // 기본 생성자 추가
@AllArgsConstructor // 모든 필드를 받는 생성자 추가
public class ReviewItemDTO {
    private String itemName; // 평가 항목 이름 (예: "응답이 빨라요")
    private boolean selected; // 해당 항목이 선택되었는지 여부
}
