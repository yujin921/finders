package net.datasa.finders.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.BoardDTO;
import net.datasa.finders.domain.entity.*;
import net.datasa.finders.repository.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
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
    private final Board_WorkScopeRepository workScopeRepository;
    private final Board_CategoryRepository categoryRepository;
    private final Board_SkillRepository skillRepository;

    public void write(BoardDTO boardDTO, List<String> selectedWorkScopes, List<String> selectedCategories, MultipartFile imageFile) {
        MemberEntity memberEntity = memberRepository.findById(boardDTO.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("회원 아이디가 없습니다."));

        // 이미지 Base64 인코딩 처리
        String imageBase64 = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imageBase64 = convertToBase64(imageFile);  // Base64로 변환된 이미지 저장
        }

        // BoardEntity 생성 후 저장
        BoardEntity boardEntity = BoardEntity.builder()
                .clientId(memberEntity)
                .projectTitle(boardDTO.getProjectTitle())
                .recruitDeadline(boardDTO.getRecruitDeadline())
                .projectStartDate(boardDTO.getProjectStartDate())
                .projectEndDate(boardDTO.getProjectEndDate())
                .projectBudget(boardDTO.getProjectBudget())
                .projectDescription(boardDTO.getProjectDescription())
                .projectImage(imageBase64) // Base64로 변환된 이미지 저장
                .build();
        boardRepository.save(boardEntity);

        // 업무 범위 저장 로직
        for (String workScope : boardDTO.getSelectedWorkScopes()) {
            Board_WorkScopeEntity workScopeEntity = Board_WorkScopeEntity.builder()
                    .boardEntity(boardEntity)
                    .workType(workScope)
                    .build();
            workScopeRepository.save(workScopeEntity);
        }

        // 카테고리 저장 로직
        for (String category : boardDTO.getSelectedCategories()) {
            Board_CategoryEntity categoryEntity = Board_CategoryEntity.builder()
                    .boardEntity(boardEntity)
                    .category(category)
                    .build();
            categoryRepository.save(categoryEntity);
        }

        // 관련 기술 저장 로직
        for (String skill : boardDTO.getSelectedSkills()) {
            Board_SkillEntity skillEntity = Board_SkillEntity.builder()
                    .boardEntity(boardEntity)
                    .skillText(skill)
                    .build();
            skillRepository.save(skillEntity);
        }
    }

    // Base64 변환 유틸리티 메서드
    private String convertToBase64(MultipartFile file) {
        try {
            byte[] fileContent = file.getBytes(); // MultipartFile을 바이트 배열로 변환
            return Base64.getEncoder().encodeToString(fileContent); // Base64로 인코딩
        } catch (IOException e) {
            throw new RuntimeException("파일을 Base64로 변환하는 데 실패했습니다.", e);
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
                .projectImage(entity.getProjectImage())  // 조회 시 Base64로 저장된 이미지 데이터
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
