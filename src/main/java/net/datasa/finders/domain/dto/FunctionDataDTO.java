package net.datasa.finders.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FunctionDataDTO {
	private String someProperty;

    public String getSomeProperty() {
        return someProperty != null ? someProperty : ""; // null을 빈 문자열로 변환
    }
}
