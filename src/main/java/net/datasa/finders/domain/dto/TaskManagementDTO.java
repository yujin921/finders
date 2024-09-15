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
    private Integer functionTitleId; // 기존 기능 선택 시
    private String functionTitleName; // 새 기능 입력 시
    private String taskTitle;
    private String taskDescription;
    private String taskStatus;
    private String taskPriority;
    private String taskStartDate;
    private String taskEndDate;
}
