package net.datasa.finders.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "team", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"project_num", "member_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamEntity {
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 @Column(name = "team_num", nullable = false)
 private int teamNum;  // team_num을 기본 키로 사용

 @Column(name = "project_num", nullable = false)
 private int projectNum;

 @Column(name = "member_id", nullable = false)
 private String memberId;

 // ProjectEntity와의 관계 설정
 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "project_num", referencedColumnName = "project_num", insertable = false, updatable = false)
 private ProjectEntity project;

 // MemberEntity와의 관계 설정 (추가)
 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "member_id", referencedColumnName = "member_id", insertable = false, updatable = false)
 private MemberEntity member;
}