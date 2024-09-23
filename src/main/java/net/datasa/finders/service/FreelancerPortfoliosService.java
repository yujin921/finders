package net.datasa.finders.service;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.datasa.finders.domain.dto.FreelancerPortfoliosDTO;
import net.datasa.finders.domain.entity.FreelancerEntity;
import net.datasa.finders.domain.entity.FreelancerPortfoliosEntity;
import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.repository.FreelancerPortfoliosRepository;
import net.datasa.finders.repository.FreelancerRepository;
import net.datasa.finders.repository.MemberRepository;
import net.datasa.finders.security.AuthenticatedUser;

@Service
@Transactional
@RequiredArgsConstructor
public class FreelancerPortfoliosService {
	
	private final FreelancerPortfoliosRepository freelancerPortfoliosRepository;
	private final FreelancerRepository freelancerRepository;
	private final MemberRepository memberRepository;
	
	public void save(FreelancerPortfoliosDTO fPDTO, AuthenticatedUser user) {
		
		MemberEntity memberEntity = memberRepository.findById(user.getUsername()).orElseThrow(() -> new EntityNotFoundException("회원정보가 없습니다."));
		
		FreelancerPortfoliosEntity freelancerPortfoliosEntity = FreelancerPortfoliosEntity.builder()
				.projectTitle(fPDTO.getProjectTitle())
				.projectDescription(fPDTO.getProjectDescription())
				.projectLink(fPDTO.getProjectLink())
				.memberEntity(memberEntity)
				.build();
		
		freelancerPortfoliosRepository.save(freelancerPortfoliosEntity);
	}

}
