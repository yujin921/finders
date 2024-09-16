package net.datasa.finders.domain.dto;

import java.time.LocalDate;

public class TaskDateRangeDTO {
	private LocalDate earliestStartDate;
    private LocalDate latestEndDate;

    // 기본 생성자
    public TaskDateRangeDTO() {}

    public TaskDateRangeDTO(LocalDate earliestStartDate, LocalDate latestEndDate) {
        this.earliestStartDate = earliestStartDate;
        this.latestEndDate = latestEndDate;
    }

    // Getter 및 Setter
    public LocalDate getEarliestStartDate() {
        return earliestStartDate;
    }

    public void setEarliestStartDate(LocalDate earliestStartDate) {
        this.earliestStartDate = earliestStartDate;
    }

    public LocalDate getLatestEndDate() {
        return latestEndDate;
    }

    public void setLatestEndDate(LocalDate latestEndDate) {
        this.latestEndDate = latestEndDate;
    }
}
