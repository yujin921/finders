package net.datasa.finders.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.datasa.finders.domain.dto.MemberDTO;
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
    
    public void join(MemberDTO dto) {

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

        memberRepository.save(entity);
    }
}
