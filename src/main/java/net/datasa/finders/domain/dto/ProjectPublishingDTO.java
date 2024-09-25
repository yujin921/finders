package net.datasa.finders.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.datasa.finders.domain.entity.RoleName;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectPublishingDTO {
    private Integer projectNum;
    private String clientId;
    private String projectTitle;
    private LocalDateTime recruitDeadline;
    private LocalDate projectStartDate;
    private LocalDate projectEndDate;
    private BigDecimal projectBudget;
    private String projectDescription;
    private MultipartFile projectImageFile;
    private String projectImage;
    private Boolean projectStatus;
    private List<String> selectedSkills;
    private List<String> selectedCategories;
    private List<String> selectedWorkScopes;
    private List<Map<String, Object>> outputList;
    private List<Integer> requiredNum;
    private List<String> prequalificationQuestions;
    private long estimatedDay;
    private RoleName roleName;
    private LocalDateTime projectCreateDate;
    private float averageRating;
}
