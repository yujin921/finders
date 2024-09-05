package net.datasa.finders.domain.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 회원정보 Entity
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "member")
public class MemberEntity {
    @Id
    @Column(name = "member_id", length = 20)
    String memberId;

    @Column(name = "member_password", nullable = false, length = 100)
    String memberPw;

    @Column(name = "member_name", nullable = false, length = 100)
    String memberName;
    
    @Lob  // 대용량 텍스트 필드를 나타내는 어노테이션
    @Column(name = "profile_img", columnDefinition = "MEDIUMTEXT")
    private String profileImg;
    
    @Column(name = "email", nullable = false, length = 100)
    String email;

    @Column(name = "enabled", nullable = false, columnDefinition = "tinyint(1) default 0")
    boolean enabled;

    @Enumerated(EnumType.STRING)
    @Column(name = "rolename", nullable = false)
    RoleName roleName;
    
    @CreatedDate
    @Column(name = "created_time", columnDefinition = "timestamp default current_timestamp")
    private LocalDateTime createdTime;
}