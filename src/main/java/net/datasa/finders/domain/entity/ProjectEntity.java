package net.datasa.finders.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

//채팅 전용
//채팅에서 project_publishing 테이블을 사용할 때 쓰는 entity, 따로 만들었음

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
    @ManyToMany(mappedBy = "projects")
    private Set<MemberEntity> members = new HashSet<>();
}
