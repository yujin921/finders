package net.datasa.finders.domain.entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;

@Entity
@Table(name = "CHAT_TEXT")
@Data
@Builder
public class ChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MESSAGE_ID")
    private int messageId;

    @Column(name = "CHATROOM_ID", nullable = false)
    private int chatroomId;

    @Column(name = "SENDER_ID", length = 50, nullable = false)
    private String senderId;

    @Column(name = "MESSAGE_CONTENTS", nullable = false, length = 1000)
    private String messageContents;

    @Column(name = "SEND_TIME", nullable = false)
    private Timestamp sendTime; // 필드 이름을 `sendTime`으로 유지
}
