package net.datasa.finders.domain.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
}
