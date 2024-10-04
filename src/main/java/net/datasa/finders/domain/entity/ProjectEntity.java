package net.datasa.finders.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
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
    
    @Column(name = "project_status", columnDefinition = "tinyint(1) default 0")
    private boolean projectStatus;

    // MemberEntity와의 다대다 관계 설정
    @ManyToMany
    @JoinTable(
        name = "team", // 조인 테이블 이름
        joinColumns = @JoinColumn(name = "project_num"), // 프로젝트 번호와 연결
        inverseJoinColumns = @JoinColumn(name = "member_id") // 멤버 ID와 연결
    )
    private Set<MemberEntity> members = new HashSet<>();

    // TeamEntity와의 일대다 관계 유지
    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
    private Set<TeamEntity> teams = new HashSet<>();
}
