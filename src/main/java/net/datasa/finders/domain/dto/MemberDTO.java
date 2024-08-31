package net.datasa.finders.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {
    String memberId;
    String memberPw;
    String memberName;
    String memberPhone;
    String memberEmail;
    boolean enabled;
    String roleName;
}
