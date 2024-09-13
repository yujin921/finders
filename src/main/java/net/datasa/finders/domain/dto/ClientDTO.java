package net.datasa.finders.domain.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientDTO {

	String clientId;
	String clientPhone;
	String industry;
	LocalDate foundedDate;
	Integer employeeCount;
	String website;
	String postalCode;
	String address;
	String detailAddress;
	String extraAddress;
}
