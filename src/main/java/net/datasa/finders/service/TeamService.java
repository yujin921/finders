package net.datasa.finders.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.repository.TeamRepository;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class TeamService {
    private final TeamRepository teamRepository;

    // roomId를 기반으로 projectNum을 추출하는 로직
    public int getProjectNumByRoomId(String roomId) {
        try {
            return Integer.parseInt(roomId);  // roomId가 projectNum과 동일한 경우
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid room ID");
        }
    }

    public boolean isTeamMember(int projectNum, String memberId) {
        return teamRepository.existsByProjectNumAndMemberId(projectNum, memberId);
    }
}
