package net.datasa.finders.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.datasa.finders.domain.entity.MemberEntity;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FreelancerDTO {
	String freelancerId;
	String freelancerPhone;
	String address;
	String postalCode;
	String country;
	MemberEntity member;
}
