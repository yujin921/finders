package net.datasa.finders.domain.dto;

import java.time.LocalDateTime;

import org.springframework.web.multipart.MultipartFile;

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
    String email;
    MultipartFile profileImg;
    String profileImgName;
    boolean enabled;
    RoleName roleName;
    LocalDateTime createdTime;
    LocalDateTime updatedTime;

    ClientDTO client;
    FreelancerDTO freelancer;
    
    // 매개변수를 가지는 생성자 추가
    public MemberDTO(String memberId, String memberPw, String memberName, String email, MultipartFile profileImg,
                     boolean enabled, RoleName roleName, LocalDateTime createdTime, LocalDateTime updatedTime) {
        this.memberId = memberId;
        this.memberPw = memberPw;
        this.memberName = memberName;
        this.email = email;
        this.profileImg = profileImg;
        this.enabled = enabled;
        this.roleName = roleName;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
    }
}
