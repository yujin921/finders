package net.datasa.finders.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.BoardDTO;
import net.datasa.finders.domain.entity.BoardEntity;
import net.datasa.finders.domain.entity.Board_SkillEntity;
import net.datasa.finders.domain.entity.Board_WorkScopeEntity;
import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.repository.BoardRepository;
import net.datasa.finders.repository.Board_SkillRepository;
import net.datasa.finders.repository.Board_WorkScopeRepository;
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

    private final Board_WorkScopeRepository boardWorkScopeRepository;
    private final Board_SkillRepository boardSkillRepository;
	private final BoardRepository boardRepository;
	private final MemberRepository memberRepository;

    public void write(BoardDTO boardDTO, List<String> workScopes, List<String> skills) {
        MemberEntity memberEntity = memberRepository.findById(boardDTO.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("회원아이디가 없습니다."));

        BoardEntity boardEntity = new BoardEntity();
        boardEntity.setClientId(memberEntity);
        boardEntity.setProjectTitle(boardDTO.getProjectTitle());
        boardEntity.setRecruitDeadline(boardDTO.getRecruitDeadline());
        boardEntity.setProjectStartDate(boardDTO.getProjectStartDate());
        boardEntity.setProjectEndDate(boardDTO.getProjectEndDate());
        boardEntity.setProjectBudget(boardDTO.getProjectBudget());
        boardEntity.setProjectDescription(boardDTO.getProjectDescription());
        boardEntity.setProjectImage(boardDTO.getProjectImage());
        boardEntity.setProjectStatus(boardDTO.getProjectStatus());

        log.debug("저장되는 엔티티 : {}", boardEntity);
        boardRepository.save(boardEntity);

        // Board_Work_ScopeEntity로 선택된 카테고리 저장
        for (String workScope : workScopes) {
            Board_WorkScopeEntity workScopeEntity = new Board_WorkScopeEntity();
            workScopeEntity.setBoardEntity(boardEntity);  // FK 연결
            workScopeEntity.setCategory(workScope);
            workScopeEntity.setRequiredNum(0);  // 필요 인원 설정, 필요시 수정 가능
            boardWorkScopeRepository.save(workScopeEntity);
        }

        // Board_SkillEntity로 선택된 기술 저장
        for (String skill : skills) {
            Board_SkillEntity skillEntity = new Board_SkillEntity();
            skillEntity.setBoardEntity(boardEntity);  // FK 연결
            skillEntity.setSkillText(skill);
            boardSkillRepository.save(skillEntity);
        }
    }
	
	
    public List<BoardDTO> getList(String id) {
        
    	Sort sort = Sort.by(Sort.Direction.DESC, "projectNum");
    	
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

    /*
    public BoardEntity saveBoardImage(MultipartFile file) throws IOException {
        BoardEntity boardEntity = new BoardEntity();

        boardEntity.setProjectImage(Base64.getEncoder().encodeToString(file.getBytes()));
        return boardRepository.save(boardEntity);
    }
    */
    
    public BoardDTO getBoard(int pNum) {
        BoardEntity entity = boardRepository.findById(pNum)
                .orElseThrow(() -> new EntityNotFoundException("해당 번호의 글이 없습니다."));

        BoardDTO dto = convertToDTO(entity);

        return dto;
    }



    public void deleteBoard(int pNum) {
        boardRepository.deleteById(pNum);
    }
}
