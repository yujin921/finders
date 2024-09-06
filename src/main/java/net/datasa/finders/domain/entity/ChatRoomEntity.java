package net.datasa.finders.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int chatroomId;

    @Column(nullable = false)
    private int projectNum;

    @Column(nullable = false)
    private String chatroomName;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdTime = LocalDateTime.now();
}
