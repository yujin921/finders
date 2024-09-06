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
@Table(name = "project_required_skills")
public class Board_SkillEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "skill_num")
    Integer skillNum;

    @ManyToOne
    @JoinColumn(name = "project_num", referencedColumnName = "project_num")
    BoardEntity boardEntity;

    @Column(name = "skill_text", nullable = false)
    String skillText;
}