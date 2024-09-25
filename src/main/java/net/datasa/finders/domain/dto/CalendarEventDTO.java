package net.datasa.finders.domain.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEventDTO {
	private Integer eventId;
    private String title;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String eventType;
    private Integer projectNum; // 프로젝트 번호
}
