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

    // 프로젝트와의 관계 설정에서 중복된 논리적 이름 사용을 피하도록 확인
    @Column(name = "project_num", nullable = false)
    private int projectNum;

    @Column(nullable = false)
    private String chatroomName;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdTime = LocalDateTime.now();

    // 만약 ProjectEntity와의 관계가 필요하다면 여기에 설정
    // ProjectEntity와의 연관관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_num", insertable = false, updatable = false)
    private ProjectEntity project;
}

