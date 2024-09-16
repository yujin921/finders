package net.datasa.finders.domain.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectManagementDTO {
	private Integer managementNum;
	private Integer projectNum;
    private LocalDate actualStartDate;
    private LocalDate actualEndDate;
    private Boolean delayedStatus;
    private Integer delayedDate;
}
