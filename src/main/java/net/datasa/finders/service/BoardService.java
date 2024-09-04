package net.datasa.finders.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.BoardDTO;
import net.datasa.finders.domain.entity.BoardEntity;
import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.repository.BoardRepository;
import net.datasa.finders.repository.MemberRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 게시판 서비스
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class BoardService {

	private final BoardRepository boardRepository;
	private final MemberRepository memberRepository;
	
	public void write(BoardDTO boardDTO) {
        MemberEntity memberEntity = memberRepository.findById(boardDTO.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("회원아이디가 없습니다."));

        BoardEntity entity = new BoardEntity();
        entity.setClientId(memberEntity);
        entity.setProjectTitle(boardDTO.getProjectTitle());
        entity.setRecruitDeadline(boardDTO.getRecruitDeadline());
        entity.setProjectStartDate(boardDTO.getProjectStartDate());
        entity.setProjectEndDate(boardDTO.getProjectEndDate());
        entity.setProjectBudget(boardDTO.getProjectBudget());
        entity.setProjectDescription(boardDTO.getProjectDescription());
        entity.setProjectImage(boardDTO.getProjectImage());
        entity.setProjectStatus(boardDTO.getProjectStatus());
        
        log.debug("저장되는 엔티티 : {}", entity);
        boardRepository.save(entity);
    }
	
	
    public List<BoardDTO> getList(String id) {
        
    	Sort sort = Sort.by(Sort.Direction.DESC, "boardNum");
    	
        //
        List<BoardEntity> entityList = boardRepository.findAll(sort);
    	
        //DTO를 저장할 리스트 생성
        List<BoardDTO> dtoList = new ArrayList<>();

        //DB에서 조회한 해당 사용자의 거래 내역을 DTO객체로 변환하여 ArrayList에 저장한다.
        for (BoardEntity entity : entityList) {
            BoardDTO dto = BoardDTO.builder()
                    .projectNum(entity.getProjectNum())
                    .clientId(entity.getClientId().getMemberId())
                    .projectTitle(entity.getProjectTitle())
                    .recruitDeadline(entity.getRecruitDeadline())
                    .projectStartDate(entity.getProjectStartDate())
                    .projectEndDate(entity.getProjectEndDate())
                    .projectBudget(entity.getProjectBudget())
                    .projectDescription(entity.getProjectDescription())
                    .projectImage(entity.getProjectImage())
                    .projectStatus(entity.getProjectStatus())
                    .build();
            dtoList.add(dto);
        }
        
        //DTO객체들이 저장된 리스트를 리턴한다.
        return dtoList;
    }
    
    /**
     * DB에서 조회한 게시글 정보인 BoardEntity 객체를 BoardDTO 객체로 변환
     * @param entity    게시글 정보 Entity 객체
     * @return          게시글 정보 DTO 개체
     */
    private BoardDTO convertToDTO(BoardEntity entity) {
        return BoardDTO.builder()
                .projectNum(entity.getProjectNum())
                .clientId(entity.getClientId().getMemberId())
                .projectTitle(entity.getProjectTitle())
                .recruitDeadline(entity.getRecruitDeadline())
                .projectStartDate(entity.getProjectStartDate())
                .projectEndDate(entity.getProjectEndDate())
                .projectBudget(entity.getProjectBudget())
                .projectDescription(entity.getProjectDescription())
                .projectImage(entity.getProjectImage())
                .projectStatus(entity.getProjectStatus())
            .build();
    }
    
    public BoardDTO getBoard(int boardNum) {
        BoardEntity entity = boardRepository.findById(boardNum)
                .orElseThrow(() -> new EntityNotFoundException("해당 번호의 글이 없습니다."));

        BoardDTO dto = convertToDTO(entity);

        
        return dto;
    }

    public void deleteBoard(int boardNum) {
        boardRepository.deleteById(boardNum);
    }
}
