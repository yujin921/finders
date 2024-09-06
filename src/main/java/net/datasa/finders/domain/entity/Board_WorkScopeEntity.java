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
public class Board_WorkScopeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_num")
    int projectNum;

    @ManyToOne
    @JoinColumn(name = "project_num", referencedColumnName = "project_num", insertable = false, updatable = false)
    BoardEntity boardEntity;

    @Column(name = "work_type", nullable = false)
    String category;

    @Column(name = "required_num", nullable = false)
    int requiredNum;
}
