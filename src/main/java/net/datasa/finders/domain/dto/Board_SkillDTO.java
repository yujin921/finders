package net.datasa.finders.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.datasa.finders.domain.entity.BoardEntity;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Board_SkillDTO {
    BoardEntity boardEntity;
    private String skillText;
}