package net.datasa.finders.domain.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.web.multipart.MultipartFile;

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
    String email;
    MultipartFile profileImg;
    String profileImgName;
    boolean enabled;
    RoleName roleName;
    LocalDateTime createdTime;
    LocalDateTime updatedTime;

    ClientDTO client;
    FreelancerDTO freelancer;
}
