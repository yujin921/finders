package net.datasa.finders.domain.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//MemberEntity.java
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

 @Column(name = "email", nullable = false, length = 100)
 private String email;
 
 @Column(name = "profile_img", columnDefinition = "mediumtext")
 private String profileImg;

 @Column(name = "enabled", nullable = false, columnDefinition = "tinyint(1) default 0")
 private boolean enabled;

 @Enumerated(EnumType.STRING)
 @Column(name = "rolename", nullable = false)
 private RoleName roleName;

 @CreatedDate
 @Column(name = "created_time", columnDefinition = "timestamp default current_timestamp")
 private LocalDateTime createdTime;
 
 @Column(name = "updated_time", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
 private LocalDateTime updatedTime;

//MemberEntity.java
@ManyToMany(fetch = FetchType.EAGER)
@JoinTable(
  name = "team",
  joinColumns = @JoinColumn(name = "member_id"),
  inverseJoinColumns = @JoinColumn(name = "project_num")
)
private Set<ProjectEntity> projects = new HashSet<>();
 
}    
