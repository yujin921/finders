package net.datasa.finders.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Table(name = "project_applications")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity
public class ProjectApplicationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_num")
    private Integer applicationNum;

    @ManyToOne
    @JoinColumn(name = "project_num", referencedColumnName = "project_num", nullable = false)
    private ProjectPublishingEntity projectNum;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "freelancer_id", referencedColumnName = "member_id", nullable = false)
    private MemberEntity freelancer;

    @Enumerated(EnumType.STRING)
    @Column(name = "application_result", nullable = false)
    private ApplicationResult applicationResult = ApplicationResult.PENDING;
}
