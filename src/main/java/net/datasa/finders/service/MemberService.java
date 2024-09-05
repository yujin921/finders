package net.datasa.finders.service;

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

    	MemberEntity entity = MemberEntity.builder()
                .memberId(dto.getMemberId())
                .memberPw(passwordEncoder.encode(dto.getMemberPw()))        // Encrypt password
                .memberName(dto.getMemberName())
                .email(dto.getEmail())
                .enabled(true) // Account enabled
                .roleName(dto.getRoleName()) // Set role from DTO
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
                    .build();
        }

        return memberRepository.save(entity);
    }
    
    public void joinFreelancer(FreelancerDTO dto, MemberEntity member) {
    	FreelancerEntity freelancerEntity = FreelancerEntity.builder()
    			.freelancerId(member.getMemberId())
    			.freelancerPhone(dto.getFreelancerPhone())
    			.address(dto.getAddress())
    			.postalCode(dto.getPostalCode())
    			.country(dto.getCountry())
    			.build();
    			freelancerRepository.save(freelancerEntity);
    }
    
    public void joinClient(ClientDTO dto, MemberEntity member) {
    	ClientEntity clientEntity = ClientEntity.builder()
    			.clientId(member.getMemberId())
    			.clientPhone(dto.getClientPhone())
    			.clientAddress(dto.getClientAddress())
    			.industry(dto.getIndustry())
    			.foundedDate(dto.getFoundedDate())
    			.employeeCount(dto.getEmployeeCount())
    			.website(dto.getWebsite())
    			.build();
    			clientRepository.save(clientEntity);
    }
}
