package net.datasa.finders.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    private MultipartFile projectImageFile;
    private String projectImage;
    private Boolean projectStatus;
    private List<String> selectedSkills;
    private List<String> selectedCategories;
    private List<String> selectedWorkScopes;
}
