// FreelancerDataDTO.java
package net.datasa.finders.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientDataDTO {
    private String id; // MemberEntity의 memberId
    private String name; // MemberEntity의 memberName
    private boolean isReviewCompleted; // 작성 완료 여부 (필요 시 사용)

    // 기존에 필요한 경우 모든 필드를 포함한 생성자 사용
    public ClientDataDTO(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
