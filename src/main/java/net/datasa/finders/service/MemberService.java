package net.datasa.finders.service;

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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional
public class MemberService {

	private final BCryptPasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final FreelancerRepository freelancerRepository;
    private final ClientRepository clientRepository;
    
    public MemberEntity join(MemberDTO dto, String uploadPath, MultipartFile profileImg) throws IOException {
        LocalDateTime now = LocalDateTime.now();
        String imageUrl = null;

        // 이미지파일 경로 설정 및 이름 저장
        File directoryPath = new File(uploadPath);
        if (!directoryPath.exists()) {
            directoryPath.mkdirs();
        } 
        if (!dto.getProfileImg().isEmpty()) {
        	 String originalName = dto.getProfileImg().getOriginalFilename();
             String extension = originalName.substring(originalName.lastIndexOf("."));
             String dateString = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
             String uuidString = UUID.randomUUID().toString();
             String fileName = dateString + "_" + uuidString + extension;

             File file = new File(uploadPath, fileName);
             dto.getProfileImg().transferTo(file);
             imageUrl = "http://localhost:8888/images/profile/" + fileName;
        } else {
        	imageUrl = "https://i.namu.wiki/i/Bge3xnYd4kRe_IKbm2uqxlhQJij2SngwNssjpjaOyOqoRhQlNwLrR2ZiK-JWJ2b99RGcSxDaZ2UCI7fiv4IDDQ.webp";
        }

        MemberEntity entity = MemberEntity.builder()
            .profileImg(imageUrl)
            .memberId(dto.getMemberId())
            .memberPw(passwordEncoder.encode(dto.getMemberPw()))
            .memberName(dto.getMemberName())
            .email(dto.getEmail())
            .enabled(true)
            .roleName(dto.getRoleName())
            .createdTime(now)
            .updatedTime(now)
            .build();

        if (dto.getMemberId().equals("admin123")) {
            entity.setRoleName(RoleName.ROLE_ADMIN);
        }
        
        return memberRepository.save(entity);
    }
    
    public boolean idCheck(String searchId) {
    	return !memberRepository.existsById(searchId);
    }

    
    public void joinFreelancer(FreelancerDTO dto, MemberDTO member) {
    	
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
    
    public void joinClient(ClientDTO dto, MemberDTO member) {
    	
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

    public MemberEntity findByMemberId(String memberId) {
        return memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("User not found with memberId: " + memberId));
    }

    // 새 메서드 사용
    public MemberDTO findByCustomMemberId(String memberId) {
        MemberEntity member = memberRepository.findByCustomMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("User not found with custom memberId: " + memberId));

        // Entity -> DTO 변환
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setMemberId(member.getMemberId());
        memberDTO.setMemberName(member.getMemberName());
        memberDTO.setRoleName(member.getRoleName());
        return memberDTO;
    }

    public MemberDTO getMemberInfo(String memberId) {
    	MemberEntity memberEntity = memberRepository.findById(memberId).orElseThrow(() -> 
    			new IllegalArgumentException("No member found with ID: " + memberId));
    	
    	//기본적으로 MemberDTO 생성
    	MemberDTO memberDTO = MemberDTO.builder()
    			.memberId(memberEntity.getMemberId())
    			.memberName(memberEntity.getMemberName())
    			.email(memberEntity.getEmail())
                .createdTime(memberEntity.getCreatedTime())
                .updatedTime(memberEntity.getUpdatedTime())
                .build();
    	
    	memberDTO.setProfileImgName(memberEntity.getProfileImg());
    	
    	if (memberEntity.getRoleName() == RoleName.ROLE_FREELANCER) {
    		freelancerRepository.findByMember(memberEntity).ifPresent(freelancerEntity -> {
    			FreelancerDTO freelancerDTO = FreelancerDTO.builder()
    					.freelancerPhone(freelancerEntity.getFreelancerPhone())
    					.country(freelancerEntity.getCountry())
    					.postalCode(freelancerEntity.getPostalCode())
    					.address(freelancerEntity.getAddress())
    					.detailAddress(freelancerEntity.getDetailAddress())
    					.extraAddress(freelancerEntity.getExtraAddress())
    					.build();
    			memberDTO.setFreelancer(freelancerDTO);
    		});
    	} else if (memberEntity.getRoleName() == RoleName.ROLE_CLIENT) {
    		clientRepository.findByMember(memberEntity).ifPresent(clientEntity -> {
    			ClientDTO clientDTO = ClientDTO.builder()
    					.clientPhone(clientEntity.getClientPhone())
    					.industry(clientEntity.getIndustry())
    					.foundedDate(clientEntity.getFoundedDate())
    					.employeeCount(clientEntity.getEmployeeCount())
    					.website(clientEntity.getWebsite())
    					.postalCode(clientEntity.getPostalCode())
    					.address(clientEntity.getAddress())
    					.detailAddress(clientEntity.getDetailAddress())
    					.extraAddress(clientEntity.getExtraAddress())
    					.build();
    			memberDTO.setClient(clientDTO);
    		});
    	}
		return memberDTO;
    }
    
    
    public void updateFreelancer(FreelancerDTO dto, MultipartFile profileImg, String uploadPath) throws IOException {
        MemberEntity member = memberRepository.findById(dto.getMemberId())
            .orElseThrow(() -> new RuntimeException("Member not found"));

        // 이름과 이메일 업데이트
        member.setMemberName(dto.getMemberName());
        member.setEmail(dto.getEmail());
       

        // 비밀번호 처리
        if (dto.getMemberPw() != null && !dto.getMemberPw().trim().isEmpty()) {
            member.setMemberPw(passwordEncoder.encode(dto.getMemberPw()));
        }

        // 프로필 이미지 처리
        String newImagePath = saveProfileImage(dto.getMemberId(), profileImg, uploadPath);
        if (newImagePath != null) {
            member.setProfileImg(newImagePath);
        }
        
        FreelancerEntity freelancer = freelancerRepository.findByMember(member)
              .orElseThrow(() -> new RuntimeException("Freelancer not found"));

          freelancer.setFreelancerPhone(dto.getFreelancerPhone());
          freelancer.setCountry(dto.getCountry());
          freelancer.setPostalCode(dto.getPostalCode());
          freelancer.setAddress(dto.getAddress());
          freelancer.setDetailAddress(dto.getDetailAddress());
          freelancer.setExtraAddress(dto.getExtraAddress());

          freelancerRepository.save(freelancer);
          memberRepository.save(member);
      }
    
    public void updateClient(ClientDTO dto, MultipartFile profileImg, String uploadPath) throws IOException {
        MemberEntity member = memberRepository.findById(dto.getMemberId())
            .orElseThrow(() -> new RuntimeException("Member not found"));

        // 이름과 이메일 업데이트
        member.setMemberName(dto.getMemberName());
        member.setEmail(dto.getEmail());
       

        // 비밀번호 처리
        if (dto.getMemberPw() != null && !dto.getMemberPw().trim().isEmpty()) {
            member.setMemberPw(passwordEncoder.encode(dto.getMemberPw()));
        }

        // 프로필 이미지 처리
        String newImagePath = saveProfileImage(dto.getMemberId(), profileImg, uploadPath);
        if (newImagePath != null) {
            member.setProfileImg(newImagePath);
        }
        
        ClientEntity client = clientRepository.findByMember(member)
              .orElseThrow(() -> new RuntimeException("client not found"));

      client.setClientPhone(dto.getClientPhone());
      client.setIndustry(dto.getIndustry());
      client.setFoundedDate(dto.getFoundedDate());
      client.setEmployeeCount(dto.getEmployeeCount());
      client.setWebsite(dto.getWebsite());
      client.setPostalCode(dto.getPostalCode());
      client.setAddress(dto.getAddress());
      client.setDetailAddress(dto.getDetailAddress());
      client.setExtraAddress(dto.getExtraAddress());

          clientRepository.save(client);
          memberRepository.save(member);
      }

        	
    private String saveProfileImage(String memberId, MultipartFile file, String uploadPath) throws IOException {
        if (file == null || file.isEmpty()) {
            return null; // 새 파일이 없으면 null 반환
        }

        // 기존 이미지 확인
        MemberEntity member = memberRepository.findById(memberId)
            .orElseThrow(() -> new RuntimeException("Member not found"));
        String oldImagePath = member.getProfileImg();

        // 새 파일 저장
        String originalName = file.getOriginalFilename();
        String extension = originalName.substring(originalName.lastIndexOf("."));
        String dateString = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuidString = UUID.randomUUID().toString();
        String newFileName = dateString + "_" + uuidString + extension;

        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        File newFile = new File(uploadDir, newFileName);
        file.transferTo(newFile);

        // 기존 파일 삭제
        if (oldImagePath != null && !oldImagePath.isEmpty()) {
            File oldFile = new File(uploadPath, oldImagePath);
            if (oldFile.exists()) {
                oldFile.delete();
            }
        }

        return "http://localhost:8888/images/profile/" + newFileName;
    }
}
