package net.datasa.finders.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FreelancerDataDTO {
    private String id; // MemberEntity의 memberId
    private String name; // MemberEntity의 memberName
}
