package net.datasa.finders.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewItemDTO {
    private String itemName;  // 체크박스 항목 이름
    private boolean selected;  // 선택 여부
}

