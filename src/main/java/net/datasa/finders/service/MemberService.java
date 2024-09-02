package net.datasa.finders.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.datasa.finders.domain.dto.MemberDTO;
import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.repository.MemberRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Transactional
public class MemberService {

    public void join(MemberDTO dto) {

        private final BCryptPasswordEncoder passwordEncoder;

        private final MemberRepository memberRepository;

        MemberEntity entity = MemberEntity.builder()
                .memberId(dto.getMemberId())
                .memberPw(passwordEncoder.encode(dto.getMemberPw()))        //비밀번호는 암호화
                .memberName(dto.getMemberName())
                .email(dto.getEmail())

                .build();

        memberRepository.save(entity);
    }
}
