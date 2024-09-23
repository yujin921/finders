package net.datasa.finders.domain.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "task_management")
public class TaskManagementEntity {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
	private Integer taskId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_num", referencedColumnName = "project_num")
    private ProjectPublishingEntity projectPublishingEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freelancer_id", referencedColumnName = "member_id")
    private MemberEntity memberEntity;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "function_title_id", referencedColumnName = "function_title_id")
    private FunctionTitleEntity functionTitleEntity;
    
    @Column(name = "task_title", nullable = false, length = 100)
    private String taskTitle;
    
    @Column(name = "task_description", nullable = false, length = 100)
    private String taskDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_status", nullable = false)
    private TaskStatus taskStatus;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "task_priority", nullable = false)
    private TaskPriority taskPriority;
	
	@Column(name = "task_start_date", nullable = false)
    private LocalDate taskStartDate;
	
	@Column(name = "task_end_date", nullable = false)
    private LocalDate taskEndDate;
	
	@Column(name = "actual_start_date")
    private LocalDate actualStartDate;
	
	@Column(name = "actual_end_date")
    private LocalDate actualEndDate;
    
	@Column(name = "task_processivity", nullable = false, columnDefinition = "DEFAULT '0%'")
    private String taskProcessivity;
	
	@Override
    public String toString() {
        return "TaskManagementEntity{" +
                "taskId=" + taskId +
                ", taskTitle='" + taskTitle + '\'' +
                ", functionTitleId=" + (functionTitleEntity != null ? functionTitleEntity.getFunctionTitleId() : null) +
                '}';
    }
	
}
