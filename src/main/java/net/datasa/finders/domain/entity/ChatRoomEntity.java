package net.datasa.finders.domain.entity;

import java.sql.Timestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CHAT_ROOM")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor // 기본 생성자를 추가하여 Hibernate에서 엔티티를 생성할 수 있도록 합니다.
public class ChatRoomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHATROOM_ID")
    private int chatroomId;

    @Column(name = "PROJECT_NUM", nullable = false)
    private int projectNum;

    @Column(name = "CHATROOM_NAME", nullable = false, length = 100)
    private String chatroomName;

    @Column(name = "CREATED_TIME", nullable = false)
    private Timestamp createdTime;

    // 추가적인 커스텀 생성자 필요 시 @Builder.Default 어노테이션을 활용해 필드를 초기화할 수 있음
    @Builder
    public ChatRoomEntity(int projectNum, String chatroomName, Timestamp createdTime) {
        this.projectNum = projectNum;
        this.chatroomName = chatroomName;
        this.createdTime = createdTime;
    }
}
