package net.datasa.finders.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FunctionTitleWithTaskIdDTO {
    private FunctionTitleDTO functionTitle; // FunctionTitleDTO 객체
    private Integer taskId; // taskId
}
