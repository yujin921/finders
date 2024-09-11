package net.datasa.finders.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.datasa.finders.domain.entity.ProjectPublishingEntity;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkScopeDTO {
    ProjectPublishingEntity projectPublishingEntity;
    private String  workType;
    private Integer requiredNum;
}