package net.datasa.finders.domain.entity;

import java.io.Serializable;
import java.util.Objects;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatParticipantId implements Serializable {

    private int chatroomId;
    private String participantId;

    // 기본 생성자 (명시적으로 public 생성자 추가)
    public ChatParticipantId() {}

    // 파라미터가 있는 생성자
    public ChatParticipantId(int chatroomId, String participantId) {
        this.chatroomId = chatroomId;
        this.participantId = participantId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatParticipantId that = (ChatParticipantId) o;
        return chatroomId == that.chatroomId && Objects.equals(participantId, that.participantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatroomId, participantId);
    }
}
