package net.datasa.finders.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

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

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime joinedTime = LocalDateTime.now();
}

