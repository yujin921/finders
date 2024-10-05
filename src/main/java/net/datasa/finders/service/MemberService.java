package net.datasa.finders.service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.ClientDTO;
import net.datasa.finders.domain.dto.FreelancerDTO;
import net.datasa.finders.domain.dto.MemberDTO;
import net.datasa.finders.domain.entity.ClientCategoryEntity;
import net.datasa.finders.domain.entity.ClientEntity;
import net.datasa.finders.domain.entity.ClientFieldEntity;
import net.datasa.finders.domain.entity.FreelancerEntity;
import net.datasa.finders.domain.entity.FreelancerSkillEntity;
import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.domain.entity.RoleName;
import net.datasa.finders.repository.ClientCategoryRepository;
import net.datasa.finders.repository.ClientFieldRepository;
import net.datasa.finders.repository.ClientRepository;
import net.datasa.finders.repository.FreelancerRepository;
import net.datasa.finders.repository.FreelancerSkillRepository;
import net.datasa.finders.repository.MemberRepository;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class MemberService {

	private final BCryptPasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final FreelancerRepository freelancerRepository;
    private final ClientRepository clientRepository;
    private final FreelancerSkillRepository freelancerSkillRepository;
    private final ClientFieldRepository clientFieldRepository;
    private final ClientCategoryRepository clientCategoryRepository;
    
    public MemberEntity join(MemberDTO dto, String uploadPath, MultipartFile profileImg) throws IOException {
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
    	
    	MemberEntity memberEntity = memberRepository.findById(member.getMemberId()).orElseThrow(() -> new EntityNotFoundException("아이디 못찾음"));
    	
    	FreelancerEntity freelancerEntity = FreelancerEntity.builder()
    			.member(memberEntity)
    			.freelancerPhone(dto.getFreelancerPhone())
    			.country(dto.getCountry())
    			.postalCode(dto.getPostalCode())
    			.address(dto.getAddress())
    			.detailAddress(dto.getDetailAddress())
    			.extraAddress(dto.getExtraAddress())
    			.build();
    			freelancerRepository.save(freelancerEntity);
    }
    

    // 문자열 정제 메서드
    private String cleanSkillString(String skill) {
        // 따옴표, 대괄호, 쉼표 등을 제거하고 앞뒤 공백을 제거
        return skill.replaceAll("[\"\\[\\],]", "").trim();
    }
    
    private String cleanFieldString(String field) {
        // 따옴표, 대괄호, 쉼표 등을 제거하고 앞뒤 공백을 제거
        return field.replaceAll("[\"\\[\\],]", "").trim();
    }
    
    private String cleanCategoryString(String category) {
        // 따옴표, 대괄호, 쉼표 등을 제거하고 앞뒤 공백을 제거
        return category.replaceAll("[\"\\[\\],]", "").trim();
    }
    
    @Transactional
    public void updateFreelancerSkills(String freelancerId, List<String> skills) {
        MemberEntity freelancer = memberRepository.findById(freelancerId)
            .orElseThrow(() -> new EntityNotFoundException("Freelancer not found"));

        // 기존 스킬 삭제
        freelancerSkillRepository.deleteByFreelancerId(freelancer);
        // 새로운 스킬 저장
        for (String skill : skills) {
            // 문자열 정제
            String cleanedSkill = cleanSkillString(skill);
            
            if (!cleanedSkill.isEmpty()) {
                FreelancerSkillEntity skillEntity = FreelancerSkillEntity.builder()
                    .freelancerId(freelancer)
                    .skillText(cleanedSkill)
                    .build();
                freelancerSkillRepository.save(skillEntity);
            }
        }
    }
    
    @Transactional
    public void updateClientField(String clientId, List<String> fields) {
        MemberEntity client = memberRepository.findById(clientId)
            .orElseThrow(() -> new EntityNotFoundException("Client not found"));

        // 기존 스킬 삭제
        clientFieldRepository.deleteByClientId(client);
        // 새로운 스킬 저장
        for (String field : fields) {
            // 문자열 정제
            String cleanedSkill = cleanFieldString(field);
            
            if (!cleanedSkill.isEmpty()) {
                ClientFieldEntity fieldEntity = ClientFieldEntity.builder()
                	.clientId(client)
                	.fieldText(cleanedSkill)
                	.build();
                clientFieldRepository.save(fieldEntity);
            }
        }
    }
    
    @Transactional
    public void updateClientCategory(String clientId, List<String> categorys) {
        MemberEntity client = memberRepository.findById(clientId)
            .orElseThrow(() -> new EntityNotFoundException("Client not found"));

        // 기존 스킬 삭제
        clientCategoryRepository.deleteByClientId(client);
        // 새로운 스킬 저장
        for (String category : categorys) {
            // 문자열 정제
            String cleanedCategory = cleanCategoryString(category);
            
            if (!cleanedCategory.isEmpty()) {
                ClientCategoryEntity categoryEntity = ClientCategoryEntity.builder()
                	.clientId(client)
                	.categoryText(cleanedCategory)
                	.build();
                clientCategoryRepository.save(categoryEntity);
            }
        }
    }

    
    public void joinClient(ClientDTO dto, MemberDTO member) {
    	
    	MemberEntity memberEntity = memberRepository.findById(member.getMemberId()).orElseThrow(() -> new EntityNotFoundException("아이디 못찾음"));
    	
    	ClientEntity clientEntity = ClientEntity.builder()
    			.member(memberEntity)
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
          
          updateFreelancerSkills(dto.getMemberId(), dto.getSkills());

          // 추가: Field와 Category 업데이트
          if (dto.getFields() != null && !dto.getFields().isEmpty()) {
              updateClientField(dto.getMemberId(), dto.getFields());
          }
          if (dto.getCategorys() != null && !dto.getCategorys().isEmpty()) {
              updateClientCategory(dto.getMemberId(), dto.getCategorys());
          }

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
        
        if (dto.getFields() != null && !dto.getFields().isEmpty()) {
            updateClientField(dto.getMemberId(), dto.getFields());
        }
        if (dto.getCategorys() != null && !dto.getCategorys().isEmpty()) {
            updateClientCategory(dto.getMemberId(), dto.getCategorys());
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
      
      updateClientField(dto.getMemberId(), dto.getFields());
      updateClientCategory(dto.getMemberId(), dto.getCategorys());

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
    

    public String findUsernameBymemberNameAndEmail(String memberName, String email) throws Exception {
        Optional<MemberEntity> member = memberRepository.findBymemberNameAndEmail(memberName, email);
        
        if (member.isEmpty()) {
            throw new Exception("사용자를 찾을 수 없습니다.");
        }

        return member.get().getMemberId(); // 아이디 반환
    }
    
    public boolean verifyUser(String memberId, String memberName, String email) {
        log.info("Verifying user in service: id={}, name={}, email={}", memberId, memberName, email);
        boolean result = memberRepository.findByMemberIdAndMemberNameAndEmail(memberId, memberName, email).isPresent();
        log.info("Verification result from repository: {}", result);
        return result;
    }
    
    public boolean resetPassword(String memberId, String newPassword) {
        Optional<MemberEntity> memberOpt = memberRepository.findByMemberId(memberId);
        
        if (memberOpt.isPresent()) {
            MemberEntity member = memberOpt.get();
            member.setMemberPw(passwordEncoder.encode(newPassword));
            memberRepository.save(member);
            return true;
        }
        return false;
    }

	public int countFreelancer() {
		List<MemberEntity> memberEntityList = memberRepository.findByRoleName(RoleName.ROLE_FREELANCER);
		
		int i = 0;
		
		for (MemberEntity memeberEntity : memberEntityList) {
			i++;
		}
		return i;
	}
	

    // 프로필 이미지를 조회하는 메서드
    public String getProfileImageById(String memberId) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with ID: " + memberId));
        
        return member.getProfileImg(); // 멤버의 프로필 이미지 경로 반환
    }
}