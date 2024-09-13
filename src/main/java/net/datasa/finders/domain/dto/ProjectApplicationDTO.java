package net.datasa.finders.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.datasa.finders.domain.entity.ApplicationResult;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectApplicationDTO {
    private int applicationNum;
    private int projectNum;
    private String freelancerId;
    private ApplicationResult applicationResult;
}
