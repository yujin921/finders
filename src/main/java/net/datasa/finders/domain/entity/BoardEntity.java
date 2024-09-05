package net.datasa.finders.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 판매글 Entity
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "project_publishing")
public class BoardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_num")
    private Integer projectNum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", referencedColumnName = "member_id")
    private MemberEntity clientId;

    @Column(name = "project_title", length = 100, nullable = false)
    private String projectTitle;

    @Column(name = "recruit_deadline", nullable = false)
    private LocalDateTime recruitDeadline;

    @Column(name = "project_start_date", nullable = false)
    private LocalDateTime projectStartDate;

    @Column(name = "project_end_date", nullable = false)
    private LocalDateTime projectEndDate;

    @Column(name = "project_budget", nullable = false, precision = 10, scale = 2)
    private BigDecimal projectBudget;

    @Column(name = "project_description", columnDefinition = "TEXT", nullable = false)
    private String projectDescription;

    @Column(name = "project_image", length = 255)
    private String projectImage;
    

    @Column(name = "project_status", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean projectStatus;
}