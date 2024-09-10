package net.datasa.finders.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDTO {
    private int projectNum;      // 프로젝트 번호
    private String projectName;  // 프로젝트 이름
}
