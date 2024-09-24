package net.datasa.finders.domain.dto;

import java.time.LocalDateTime;

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
    private LocalDateTime actualStartDate;
    private LocalDateTime actualEndDate;
    private Boolean delayedStatus;
    private Integer delayedDate;
}
