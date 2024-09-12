package net.datasa.finders.domain.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.datasa.finders.domain.entity.TaskPriority;
import net.datasa.finders.domain.entity.TaskStatus;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskManagementDTO {
	private Integer taskId;
    private Integer projectNum;
    private String freelancerId;
    private String functionTitle;
    private String taskTitle;
    private String taskDescription;
    private TaskStatus taskStatus;
    private TaskPriority taskPriority;
    private LocalDate taskStartDate;
    private LocalDate taskEndDate;
}
