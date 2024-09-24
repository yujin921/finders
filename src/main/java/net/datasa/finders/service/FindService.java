package net.datasa.finders.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

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
import net.datasa.finders.repository.FreelancerPortfoliosRepository;
import net.datasa.finders.repository.FreelancerReviewsRepository;
import net.datasa.finders.repository.FreelancerSkillRepository;
import net.datasa.finders.repository.MemberRepository;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FindService {

	private final MemberRepository memberRepository;
	private final FreelancerReviewsRepository freelancerReviewsRepository;
	private final FreelancerSkillRepository freelancerSkillRepository;
	private final FreelancerPortfoliosRepository freelancerPortfoliosRepository;
	
	public List<FindFreelancerDTO> findFreelancerList(String[] fields, String[] areas) {

		List<MemberEntity> memberEntityList = memberRepository.findByRoleName(RoleName.ROLE_FREELANCER);
		ArrayList<FindFreelancerDTO> findFreelancerDTOList = new ArrayList<>();
		
		for (MemberEntity memberEntity : memberEntityList) {
			
			double totalRating = 0;
			
			List<FreelancerReviewsEntity> freelancerReviewsEntityList = freelancerReviewsRepository.findByFreelancerId(memberEntity.getMemberId());
			
			for (FreelancerReviewsEntity freelancerReviewsEntity : freelancerReviewsEntityList) {
				FreelancerReviewDTO freelancerReviewDTO = FreelancerReviewDTO.builder()
						.rating(freelancerReviewsEntity.getRating())
						.build();
				totalRating += freelancerReviewDTO.getRating();
			}
			totalRating /= freelancerReviewsEntityList.size();
			
			if(Double.isNaN(totalRating)) {
				totalRating = 0;
			}
			
			List<FreelancerSkillEntity> freelancerSkillEntityList = freelancerSkillRepository.findByFreelancerId(memberEntity);

			String[] skills = new String[freelancerSkillEntityList.size()];
			int i = 0;
			for (FreelancerSkillEntity freelancerSkillEntity : freelancerSkillEntityList) {
				skills[i] = freelancerSkillEntity.getSkillText();
				i += 1;
			}
			
			List<FreelancerPortfoliosEntity> freelancerPortfoliosEntityList = freelancerPortfoliosRepository.findByMember(memberEntity);
			
			FindFreelancerDTO findFreelancerDTO = FindFreelancerDTO.builder()
					.memberId(memberEntity.getMemberId())
					.profileImg(memberEntity.getProfileImg())
					.totalRating(totalRating)
					.totalPortfolios(freelancerPortfoliosEntityList.size())
					.totalReviews(freelancerReviewsEntityList.size())
					.skills(skills)
					.build();
			
			findFreelancerDTOList.add(findFreelancerDTO);
		}
		
		return findFreelancerDTOList;
	}
	
	
}
