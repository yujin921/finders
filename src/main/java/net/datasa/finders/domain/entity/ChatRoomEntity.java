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
@Table(name = "CHAT_ROOM")
@Data
@Builder
public class ChatRoomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHATROOM_ID")
    private int chatroomId;

    @Column(name = "CHATROOM_NAME", nullable = false, length = 100)
    private String chatroomName;

    @Column(name = "CREATED_TIME", nullable = false)
    private Timestamp createdTime;
}
