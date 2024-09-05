package net.datasa.finders.domain.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.datasa.finders.domain.entity.MemberEntity;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor

public class ClientDTO {

	String clientId;
	String clientPhone;
	String clientAddress;
	String industry;
	LocalDate foundedDate;
	Integer employeeCount;
	String website;
	LocalDateTime updatedTime;
	MemberEntity member;
}
