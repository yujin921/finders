package net.datasa.finders.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Table(name = "chat_participant")
@Data
@NoArgsConstructor
public class ChatParticipantEntity {

    @Column(name = "CHATROOM_ID", nullable = false)
    private int chatroomId;

    @Id
    @Column(name = "PARTICIPANT_ID", length = 20, nullable = false)
    private String participantId;

    @Column(name = "JOINED_TIME", nullable = false)
    private Timestamp joinedTime;

    @Builder
    public ChatParticipantEntity(int chatroomId, String participantId, Timestamp joinedTime) {
        this.chatroomId = chatroomId;
        this.participantId = participantId;
        this.joinedTime = joinedTime;
    }
}
