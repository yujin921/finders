package net.datasa.finders.domain.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//채팅전용
@Entity
@Table(name = "chat_participant")
@IdClass(ChatParticipantId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatParticipantEntity implements Serializable {

    @Id
    private int chatroomId;

    @Id
    private String participantId;
    
    @Column(name = "last_read_time")
    private Timestamp lastReadTime;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime joinedTime = LocalDateTime.now();
}

