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
@Table(name = "freelancer_skills")
public class FreelancerSkillEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "skill_num")
    Integer skillNum;

    @ManyToOne
    @JoinColumn(name = "freelancer_id", referencedColumnName = "member_id")
    MemberEntity freelancerId;

    @Column(name = "skill_text", nullable = false)
    String skillText;
}