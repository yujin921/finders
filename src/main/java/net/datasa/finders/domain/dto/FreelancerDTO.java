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
	String country;
	String postalCode;
	String address;
	String detailAddress;
	String extraAddress;
}
