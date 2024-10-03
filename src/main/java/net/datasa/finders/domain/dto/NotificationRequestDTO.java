package net.datasa.finders.domain.dto;

import lombok.Data;

@Data
public class NotificationRequestDTO {
	private String message;
    private Integer taskId;
}
