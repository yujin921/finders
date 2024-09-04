package net.datasa.finders.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 판매글 DTO
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardDTO {
    private Integer projectNum;
    private String clientId;
    private String projectTitle;
    private LocalDateTime recruitDeadline;
    private LocalDateTime projectStartDate;
    private LocalDateTime projectEndDate;
    private BigDecimal projectBudget;
    private String projectDescription;
    private String projectImage;
    private Boolean projectStatus;
}
