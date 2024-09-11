package net.datasa.finders.domain.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @ManyToOne
    @JoinColumn(name = "project_num", referencedColumnName = "project_num")
    private BoardEntity boardEntity;

    @ManyToOne
    @JoinColumn(name = "freelancer_id", referencedColumnName = "member_id")
    private MemberEntity memberEntity;
    
    @Column(name = "function_title", nullable = false, length = 100)
    private String functionTitle;
    
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
    
}
