package net.datasa.finders.domain.dto;

import java.time.LocalDateTime;

public class TaskDateRangeDTO {
	private LocalDateTime earliestStartDate;
    private LocalDateTime latestEndDate;

    // 기본 생성자
    public TaskDateRangeDTO() {}

    public TaskDateRangeDTO(LocalDateTime earliestStartDate, LocalDateTime latestEndDate) {
        this.earliestStartDate = earliestStartDate;
        this.latestEndDate = latestEndDate;
    }

    // Getter 및 Setter
    public LocalDateTime getEarliestStartDate() {
        return earliestStartDate;
    }

    public void setEarliestStartDate(LocalDateTime earliestStartDate) {
        this.earliestStartDate = earliestStartDate;
    }

    public LocalDateTime getLatestEndDate() {
        return latestEndDate;
    }

    public void setLatestEndDate(LocalDateTime latestEndDate) {
        this.latestEndDate = latestEndDate;
    }
}
