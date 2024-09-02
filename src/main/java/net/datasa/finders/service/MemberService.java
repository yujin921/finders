package net.datasa.finders.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.datasa.finders.domain.dto.MemberDTO;
import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.domain.entity.RoleName;
import net.datasa.finders.repository.MemberRepository;

@RequiredArgsConstructor
@Service
@Transactional
public class MemberService {

	private final BCryptPasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    
    public void join(MemberDTO dto) {

        MemberEntity entity = MemberEntity.builder()
                .memberId(dto.getMemberId())
                .memberPw(passwordEncoder.encode(dto.getMemberPw()))        //비밀번호는 암호화
                .memberName(dto.getMemberName())
                .email(dto.getEmail())
                .enabled(true) // 활성화 여부
                .roleName(RoleName.ROLE_CLIENT) // 역할 설정
                .build();

        memberRepository.save(entity);
    }
}
