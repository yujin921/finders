package net.datasa.finders.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//채팅 전용
// 채팅에서 project_publishing 테이블을 사용할 때 쓰는 dto, 따로 만들었음
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDTO {
    private int projectNum;      // 프로젝트 번호
    private String projectName;  // 프로젝트 이름
}
