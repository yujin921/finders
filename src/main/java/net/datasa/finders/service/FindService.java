package net.datasa.finders.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.FindFreelancerDTO;
import net.datasa.finders.domain.dto.FreelancerReviewDTO;
import net.datasa.finders.domain.entity.FreelancerPortfoliosEntity;
import net.datasa.finders.domain.entity.FreelancerReviewsEntity;
import net.datasa.finders.domain.entity.FreelancerSkillEntity;
import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.domain.entity.RoleName;
import net.datasa.finders.domain.entity.TeamEntity;
import net.datasa.finders.repository.ClientCategoryRepository;
import net.datasa.finders.repository.ClientFieldRepository;
import net.datasa.finders.repository.FreelancerPortfoliosRepository;
import net.datasa.finders.repository.FreelancerReviewsRepository;
import net.datasa.finders.repository.FreelancerSkillRepository;
import net.datasa.finders.repository.MemberRepository;
import net.datasa.finders.repository.TeamRepository;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FindService {

	private final MemberRepository memberRepository;
	private final FreelancerReviewsRepository freelancerReviewsRepository;
	private final FreelancerSkillRepository freelancerSkillRepository;
	private final FreelancerPortfoliosRepository freelancerPortfoliosRepository;
	private final ClientFieldRepository clientFieldRepository;
	private final ClientCategoryRepository clientCategoryRepository;
	private final TeamRepository teamRepository;
	
	public Page<FindFreelancerDTO> findFreelancerList(List<String> fields, List<String> areas, String search, Pageable pageable) {
		
		// 전체 회원 목록을 가져옵니다.
        List<MemberEntity> allMembers = memberRepository.findByRoleName(RoleName.ROLE_FREELANCER);

        // 필터링 및 변환 로직
        List<FindFreelancerDTO> filteredList = allMembers.stream()
            .filter(memberEntity -> 
                fields.stream().anyMatch(field -> clientFieldRepository.findByClientIdAndFieldText(memberEntity, field).isPresent()) &&
                areas.stream().anyMatch(area -> clientCategoryRepository.findByClientIdAndCategoryText(memberEntity, area).isPresent())
            )
            .map(memberEntity -> findFreelancerDetail(memberEntity.getMemberId()))
            .distinct()
            .collect(Collectors.toList());

        // 검색어로 추가 필터링 (만약 search 파라미터가 사용되고 있다면)
        if (search != null && !search.isEmpty()) {
            filteredList = filteredList.stream()
                .filter(dto -> dto.getMemberId().toLowerCase().contains(search.toLowerCase()))
                .collect(Collectors.toList());
        }

        // 페이지네이션 적용
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredList.size());
        
        List<FindFreelancerDTO> pageContent = filteredList.subList(start, end);

        return new PageImpl<>(pageContent, pageable, filteredList.size());
	}
	
	public List<FindFreelancerDTO> allFindFreelancerList(String search) {
		
		List<MemberEntity> memberEntityList = memberRepository.findByRoleNameAndMemberIdContaining(RoleName.ROLE_FREELANCER, search);
		log.debug("{}", memberEntityList);
		List<FindFreelancerDTO> findFreelancerDTOList = new ArrayList<>();
		memberEntityList.stream()
		.map(memberEntity -> findFreelancerDetail(memberEntity.getMemberId()))
		.forEach(findFreelancerDTOList::add);
		return findFreelancerDTOList;
	}


	public FindFreelancerDTO findFreelancerDetail(String memberId) {
		MemberEntity memberEntity = memberRepository.findById(memberId)
				.orElseThrow(() -> new EntityNotFoundException("아이디를 찾을 수 없습니다."));
		
		List<FreelancerReviewsEntity> freelancerReviewsEntityList = freelancerReviewsRepository.findByreceivedId(memberEntity.getMemberId());
		
		double totalRating = 0.0;
		for (FreelancerReviewsEntity freelancerReviewsEntity : freelancerReviewsEntityList) {;
			FreelancerReviewDTO freelancerReviewDTO = FreelancerReviewDTO.builder()
				.rating(freelancerReviewsEntity.getRating())
				.build();
			totalRating += freelancerReviewDTO.getRating();
		} // 모든 평점 합산
		
		totalRating /= freelancerReviewsEntityList.size();
		
		if(Double.isNaN(totalRating)) {
			totalRating = 0.0;
		} // 평점이 없을 경우 0점 입력
		
		List<FreelancerSkillEntity> freelancerSkillEntityList = freelancerSkillRepository.findByFreelancerId(memberEntity);
		
		String[] skills = new String[freelancerSkillEntityList.size()];
		int i = 0;
		for (FreelancerSkillEntity freelancerSkillEntity : freelancerSkillEntityList) {
			skills[i] = freelancerSkillEntity.getSkillText();
			i += 1;
		} // 프리랜서 보유 스킬 찾기
		
		List<FreelancerPortfoliosEntity> freelancerPortfoliosEntityList = freelancerPortfoliosRepository.findByMember(memberEntity);
		
		List<TeamEntity> teamEntityList = teamRepository.findByMember(memberEntity);
		
	
		FindFreelancerDTO findFreelancerDTO = FindFreelancerDTO.builder()
				.memberId(memberEntity.getMemberId())
				.profileImg(memberEntity.getProfileImg())
				.totalRating(totalRating)
				.totalPortfolios(freelancerPortfoliosEntityList.size())
				.totalReviews(freelancerReviewsEntityList.size())
				.totalProjects(teamEntityList.size())
				.skills(skills)
				.build();
		
		return findFreelancerDTO;
	}
	
	
}
