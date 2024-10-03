package net.datasa.finders.domain.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "task_notifications")
public class TaskNotificationsEntity {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer notificationId;

    @Column(name = "notification_message", nullable = false)
    private String notificationMessage;

    @Column(name = "read_status", columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean readStatus; // 읽음 여부

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", referencedColumnName = "member_id")
    private MemberEntity senderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", referencedColumnName = "member_id")
    private MemberEntity recipientId;

    @CreatedDate
    @Column(name = "create_date", columnDefinition = "timestamp default current_timestamp", updatable = false)
    private LocalDateTime createDate; // 생성 시각
}
