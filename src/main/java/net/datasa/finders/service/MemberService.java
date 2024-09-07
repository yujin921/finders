package net.datasa.finders.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.datasa.finders.domain.dto.ClientDTO;
import net.datasa.finders.domain.dto.FreelancerDTO;
import net.datasa.finders.domain.dto.MemberDTO;
import net.datasa.finders.domain.entity.ClientEntity;
import net.datasa.finders.domain.entity.FreelancerEntity;
import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.domain.entity.RoleName;
import net.datasa.finders.repository.ClientRepository;
import net.datasa.finders.repository.FreelancerRepository;
import net.datasa.finders.repository.MemberRepository;

@RequiredArgsConstructor
@Service
@Transactional
public class MemberService {

	private final BCryptPasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final FreelancerRepository freelancerRepository;
    private final ClientRepository clientRepository;
    
    public MemberEntity join(MemberDTO dto) {
    	
    	LocalDateTime now = LocalDateTime.now();  // 회원가입 일시, 회원정보 수정 일시
    	
    	MemberEntity entity = MemberEntity.builder()
                .memberId(dto.getMemberId())
                .memberPw(passwordEncoder.encode(dto.getMemberPw()))        // Encrypt password
                .memberName(dto.getMemberName())
                .email(dto.getEmail())
                .enabled(true) // Account enabled
                .roleName(dto.getRoleName()) // Set role from DTO
                .createdTime(now)
                .updatedTime(now)
                .build();
        
        // Special handling for admin account
        if (dto.getMemberId().equals("admin123")) {
            entity = MemberEntity.builder()
                    .memberId(dto.getMemberId())
                    .memberPw(passwordEncoder.encode(dto.getMemberPw()))        // Encrypt password
                    .memberName(dto.getMemberName())
                    .email(dto.getEmail())
                    .enabled(true) // Account enabled
                    .roleName(RoleName.ROLE_ADMIN) // Set role to ADMIN
                    .createdTime(now)
                    .updatedTime(now)
                    .build();
        }

        return memberRepository.save(entity);
    }
    
    public void joinFreelancer(FreelancerDTO dto, MemberEntity member) {
    	
    	FreelancerEntity freelancerEntity = FreelancerEntity.builder()
    			.freelancerId(member.getMemberId())
    			.freelancerPhone(dto.getFreelancerPhone())
    			.country(dto.getCountry())
    			.postalCode(dto.getPostalCode())
    			.address(dto.getAddress())
    			.detailAddress(dto.getDetailAddress())
    			.extraAddress(dto.getExtraAddress())
    			.build();
    			freelancerRepository.save(freelancerEntity);
    }
    
    public void joinClient(ClientDTO dto, MemberEntity member) {
    	
    	ClientEntity clientEntity = ClientEntity.builder()
    			.clientId(member.getMemberId())
    			.clientPhone(dto.getClientPhone())
    			.industry(dto.getIndustry())
    			.foundedDate(dto.getFoundedDate())
    			.employeeCount(dto.getEmployeeCount())
    			.website(dto.getWebsite())
    			.postalCode(dto.getPostalCode())
    			.address(dto.getAddress())
    			.detailAddress(dto.getDetailAddress())
    			.extraAddress(dto.getExtraAddress())
    			.build();
    			clientRepository.save(clientEntity);
    }
}
