package net.datasa.finders.domain.entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;

@Entity
@Table(name = "chat_participant")
@IdClass(ChatParticipantId.class)
@Data
@Builder
public class ChatParticipantEntity {

    @Id
    @Column(name = "CHATROOM_ID")
    private int chatroomId;

    @Id
    @Column(name = "PARTICIPANT_ID", length = 50)
    private String participantId;

    @Column(name = "JOINED_TIME", nullable = false)
    private Timestamp joinedTime;
}
