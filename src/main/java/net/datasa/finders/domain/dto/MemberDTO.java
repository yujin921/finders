package net.datasa.finders.domain.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.domain.entity.RoleName;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {
    String memberId;
    String memberPw;
    String memberName;
    String profileImg;
    String email;
    boolean enabled;
    RoleName roleName;
    LocalDateTime createdTime;
    LocalDateTime updatedTime;
    // FreelancerDTO 내용
    String freelancerId;
	String freelancerPhone;
	String country;
	MemberEntity member;
	// ClientDTO 내용
	String clientId;
	String clientPhone;
	String industry;
	LocalDate foundedDate;
	Integer employeeCount;
	String website;	
}
