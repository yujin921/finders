package net.datasa.finders.domain.entity;

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

    @Column(name = "member_phone", nullable = false, length = 100)
    String memberPhone;

    @Column(name = "member_email", nullable = false, length = 100)
    String memberEmail;

    @Column(name = "enabled", nullable = false, columnDefinition = "tinyint(1) default 0")
    boolean enabled;

    @Enumerated(EnumType.STRING)
    @Column(name = "rolename", nullable = false)
    String roleName;
}
