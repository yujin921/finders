package net.datasa.finders.domain.dto;

import java.time.LocalDateTime;

import lombok.Data;
import net.datasa.finders.domain.entity.TaskNotificationsEntity;

@Data
public class TaskNotificationsDTO {
	private Integer notificationId; // 알림 ID
	private String notificationMessage;
    private boolean readStatus;
    private String sender;
    private String recipient;
    private Integer task;
    private LocalDateTime createDate; // 생성 시각
    
    public static TaskNotificationsDTO fromEntity(TaskNotificationsEntity entity) {
        TaskNotificationsDTO dto = new TaskNotificationsDTO();
        dto.setNotificationId(entity.getNotificationId());
        dto.setNotificationMessage(entity.getNotificationMessage());
        dto.setReadStatus(entity.isReadStatus());
        dto.setSender(entity.getSender().getMemberId());
        dto.setRecipient(entity.getRecipient().getMemberId());
        dto.setTask(entity.getTask().getTaskId());
        dto.setCreateDate(entity.getCreateDate());
        return dto;
    }
}
