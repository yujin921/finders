package net.datasa.finders.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Table(name = "project_publishing")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_num")
    private int projectNum;

    @Column(name = "project_title", nullable = false, length = 100)
    private String projectName;

    // 멤버와의 다대다 관계 설정
    @ManyToMany(mappedBy = "projects")  // MemberEntity의 projects 필드와 매핑
    private Set<MemberEntity> members;  // 프로젝트에 참여한 멤버 목록
}
