package net.datasa.finders.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "work_scope")
public class WorkScopeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "work_scope_num")
    Integer workScopeNum;

    @ManyToOne
    @JoinColumn(name = "project_num", referencedColumnName = "project_num")
    ProjectPublishingEntity projectPublishingEntity;

    @Column(name = "work_type", nullable = false)
    String workType;

    @Column(name = "required_num", nullable = false)
    int requiredNum;
}