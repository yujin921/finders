package net.datasa.finders.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "member")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class MemberEntity {

    @Id
    @Column(name = "member_id", length = 20)
    private String memberId;

    @Column(name = "member_password", nullable = false, length = 100)
    private String memberPw;

    @Column(name = "member_name", nullable = false, length = 100)
    private String memberName;

    @Lob
    @Column(name = "profile_img", columnDefinition = "MEDIUMTEXT")
    private String profileImg;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "enabled", nullable = false, columnDefinition = "tinyint(1) default 0")
    private boolean enabled;

    @Enumerated(EnumType.STRING)
    @Column(name = "rolename", nullable = false)
    private RoleName roleName;

    @CreatedDate
    @Column(name = "created_time", columnDefinition = "timestamp default current_timestamp")
    private LocalDateTime createdTime;

    // 다대다 관계 설정, 즉시 로딩으로 변경
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "team",  // 조인 테이블 이름
            joinColumns = @JoinColumn(name = "member_id"),  // 멤버 측 조인
            inverseJoinColumns = @JoinColumn(name = "project_num")  // 프로젝트 측 조인
    )
    private Set<ProjectEntity> projects;  // 멤버가 참여한 프로젝트 목록
}
