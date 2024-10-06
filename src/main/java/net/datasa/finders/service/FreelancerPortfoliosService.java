package net.datasa.finders.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.FreelancerPortfoliosDTO;
import net.datasa.finders.domain.entity.FreelancerPortfoliosEntity;
import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.repository.FreelancerPortfoliosRepository;
import net.datasa.finders.repository.FreelancerRepository;
import net.datasa.finders.repository.MemberRepository;
import net.datasa.finders.security.AuthenticatedUser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
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
				.portfolioTitle(fPDTO.getPortfolioTitle())
				.portfolioDescription(fPDTO.getPortfolioDescription())
				.portfolioLink(fPDTO.getProjectLink())
				.member(memberEntity)
				.build();
		
		freelancerPortfoliosRepository.save(freelancerPortfoliosEntity);
	}

	public List<FreelancerPortfoliosDTO> findPortfolioList(String memberId) {
		
		MemberEntity memberEntity = memberRepository.findById(memberId).orElseThrow(() -> new EntityNotFoundException("회원정보가 없습니다."));
		
		List<FreelancerPortfoliosEntity> freelancerPortfoliosEntityList = freelancerPortfoliosRepository.findByMember(memberEntity);
		ArrayList<FreelancerPortfoliosDTO> freelancerPortfoliosDTOList = new ArrayList<>();
		
		for (FreelancerPortfoliosEntity freelancerPortfoliosEntity : freelancerPortfoliosEntityList) {
			FreelancerPortfoliosDTO freelancerPortfoliosDTO = FreelancerPortfoliosDTO.builder()
					.portfolioId(freelancerPortfoliosEntity.getPortfolioId())
					.portfolioTitle(freelancerPortfoliosEntity.getPortfolioTitle())
					.build();
			freelancerPortfoliosDTOList.add(freelancerPortfoliosDTO);
		}
		
		return freelancerPortfoliosDTOList;
	}

	public FreelancerPortfoliosDTO findPortfolioById(int portfolioId) {
		
		FreelancerPortfoliosEntity freelancerPortfoliosEntity = freelancerPortfoliosRepository.findById(portfolioId)
					.orElseThrow(() -> new EntityNotFoundException("포트폴리오정보가 없습니다."));
		
		FreelancerPortfoliosDTO freelancerPortfoliosDTO = FreelancerPortfoliosDTO.builder()
				.portfolioId(freelancerPortfoliosEntity.getPortfolioId())
				.portfolioTitle(freelancerPortfoliosEntity.getPortfolioTitle())
				.portfolioDescription(freelancerPortfoliosEntity.getPortfolioDescription())
				.build();
		
		return freelancerPortfoliosDTO;
	}

	public void deletePortfolio(int portfolioId, String id) {
		freelancerPortfoliosRepository.deleteById(portfolioId);
		log.debug("삭제완료");
		return;
	}

	public FreelancerPortfoliosDTO getPortfolioToGuest(int portfolioId) throws Exception {
		FreelancerPortfoliosEntity portfolio = freelancerPortfoliosRepository.findById(portfolioId)
				.orElseThrow(() -> new EntityNotFoundException("Portfolio not found"));

		return convertToDtoForFreelancer(portfolio);
	}

	public FreelancerPortfoliosDTO getPortfolioById(int portfolioId, String userId) throws Exception {
		FreelancerPortfoliosEntity portfolio = freelancerPortfoliosRepository.findById(portfolioId)
		        .orElseThrow(() -> new EntityNotFoundException("Portfolio not found"));

		    if (!portfolio.getMember().getMemberId().equals(userId)) {
		    	throw new Exception("You don't have permission to update this portfolio");
		    }

		    return convertToDTO(portfolio);
	}

	public FreelancerPortfoliosDTO convertToDtoForFreelancer(FreelancerPortfoliosEntity freelancerPortfoliosEntity) {

		return FreelancerPortfoliosDTO.builder()
				.portfolioId(freelancerPortfoliosEntity.getPortfolioId())
				.portfolioTitle(freelancerPortfoliosEntity.getPortfolioTitle())
				.portfolioDescription(freelancerPortfoliosEntity.getPortfolioDescription())
				.freelancerId(freelancerPortfoliosEntity.getMember().getMemberId())
				.build();
	}

	public FreelancerPortfoliosDTO convertToDTO(FreelancerPortfoliosEntity freelancerPortfoliosEntity) {
	
		return FreelancerPortfoliosDTO.builder()
				.portfolioId(freelancerPortfoliosEntity.getPortfolioId())
				.portfolioTitle(freelancerPortfoliosEntity.getPortfolioTitle())
				.portfolioDescription(freelancerPortfoliosEntity.getPortfolioDescription())
				.build();
	}

	public void updatePortfolio(FreelancerPortfoliosDTO updatedPortfolio, String userId) throws Exception {
		FreelancerPortfoliosEntity portfolio = freelancerPortfoliosRepository.findById(updatedPortfolio.getPortfolioId())
		        .orElseThrow(() -> new EntityNotFoundException("Portfolio not found"));
		    
		    if (!portfolio.getMember().getMemberId().equals(userId)) {
		        throw new Exception("You don't have permission to update this portfolio");
		    }
		    
		    // 업데이트 로직
		    portfolio.setPortfolioTitle(updatedPortfolio.getPortfolioTitle());
		    portfolio.setPortfolioDescription(updatedPortfolio.getPortfolioDescription());
		    // 기타 필요한 필드 업데이트
		    
		    freelancerPortfoliosRepository.save(portfolio);
		
	}
}
