package net.datasa.finders.service;

import java.util.ArrayList;
import java.util.List;

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
	
	public List<FindFreelancerDTO> findFreelancerList(List<String> fields, List<String> areas, String search) {
		
		List<MemberEntity> memberEntityList = memberRepository.findByRoleNameAndMemberIdContaining(RoleName.ROLE_FREELANCER, search);
		
		List<FindFreelancerDTO> findFreelancerDTOList = new ArrayList<>();
		
		/*
		 * for (MemberEntity memberEntity : memberEntityList) { for (String field :
		 * fields) { for (String area : areas) {
		 * 
		 * if(clientFieldRepository.findByClientIdAndFieldText(memberEntity,
		 * field).isPresent() &&
		 * clientCategoryRepository.findByClientIdAndCategoryText(memberEntity,
		 * area).isPresent()) {
		 * 
		 * FindFreelancerDTO findFreelancerDTO =
		 * findFreelancerDetail(memberEntity.getMemberId());
		 * 
		 * if(!findFreelancerDTOList.contains(findFreelancerDTO)) {
		 * findFreelancerDTOList.add(findFreelancerDTO); } // 프리랜서 정보가 이미 들어가 있을 시 중복
		 * 제거를 위해 조건 추가
		 * 
		 * }; }; };
		 * 
		 * }
		 */
		memberEntityList.stream()
	    .filter(memberEntity -> fields.stream()
	        .anyMatch(field -> clientFieldRepository.findByClientIdAndFieldText(memberEntity, field).isPresent()) &&
	        areas.stream()
	        .anyMatch(area -> clientCategoryRepository.findByClientIdAndCategoryText(memberEntity, area).isPresent())
	    )
	    .map(memberEntity -> findFreelancerDetail(memberEntity.getMemberId()))  // FindFreelancerDTO로 변환
	    .distinct()  // 중복 제거
	    .forEach(findFreelancerDTOList::add);
		
		return findFreelancerDTOList;
	}
	


	public FindFreelancerDTO findFreelancerDetail(String memberId) {
		MemberEntity memberEntity = memberRepository.findById(memberId)
				.orElseThrow(() -> new EntityNotFoundException("아이디를 찾을 수 없습니다."));
		
		//List<FreelancerReviewsEntity> freelancerReviewsEntityList = freelancerReviewsRepository.findByFreelancerId(memberEntity.getMemberId());
		/*
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
		*/
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
				//.totalRating(totalRating)
				.totalPortfolios(freelancerPortfoliosEntityList.size())
				//.totalReviews(freelancerReviewsEntityList.size())
				.totalProjects(teamEntityList.size())
				.skills(skills)
				.build();
		
		return findFreelancerDTO;
	}
	
	
}
