package net.datasa.finders.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "project_publishing")
public class ProjectPublishingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_num")
    private Integer projectNum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", referencedColumnName = "member_id")
    private MemberEntity clientId;

    @Column(name = "project_title", length = 100, nullable = false)
    private String projectTitle;

    @Column(name = "recruit_deadline")
    private LocalDateTime recruitDeadline;

    @Column(name = "project_start_date")
    private LocalDate projectStartDate;

    @Column(name = "project_end_date")
    private LocalDate projectEndDate;

    @Column(name = "project_budget", precision = 10, scale = 2)
    private BigDecimal projectBudget;

    @Column(name = "project_description", columnDefinition = "TEXT")
    private String projectDescription;

    @Column(name = "project_image", columnDefinition = "MEDIUMTEXT")
    private String projectImage;

    @Column(name = "project_status", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean projectStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_num", referencedColumnName = "project_num", insertable = false, updatable = false)
    private ProjectEntity project;

    @Column(name = "project_create_date", columnDefinition = "TIMESTAMP default current_timestamp")
    private LocalDateTime projectCreateDate;
}