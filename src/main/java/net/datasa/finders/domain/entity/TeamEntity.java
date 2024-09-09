package net.datasa.finders.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "team")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(TeamEntityId.class) // 복합 키 설정을 위한 IdClass 사용
public class TeamEntity {

    @Id
    @Column(name = "project_num", nullable = false)
    private Integer projectNum;

    @Id
    @Column(name = "member_id", nullable = false)
    private String memberId;

    // ProjectEntity와의 관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_num", insertable = false, updatable = false)
    private ProjectEntity project; // ProjectEntity와의 연결
}
