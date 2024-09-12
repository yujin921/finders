package net.datasa.finders.service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    
//    public MemberEntity join(MemberDTO dto, String uploadPath, MultipartFile profileImg) throws IOException {
//        
//        LocalDateTime now = LocalDateTime.now();  // 회원가입 일시, 회원정보 수정 일시
//        
//        String imageUrl;
//        
//        // 첨부파일이 있는지 확인
//        if (profileImg != null && !profileImg.isEmpty()) {
//            // 저장할 경로의 디렉토리가 있는지 확인 -> 없으면 생성
//            File directoryPath = new File(uploadPath);
//            
//            if (!directoryPath.isDirectory()) {
//                directoryPath.mkdirs();
//            }
//            
//            // 저장할 파일명 생성
//            String originalName = profileImg.getOriginalFilename();
//            String extension = originalName.substring(originalName.lastIndexOf("."));  // 확장자를 가져옴
//            String dateString = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
//            String uuidString = UUID.randomUUID().toString();
//            String fileName = dateString + "_" + uuidString + extension;
//            
//            File file = new File(uploadPath, fileName);
//            profileImg.transferTo(file);
//            
//            // 이미지가 업로드된 경우 서버 내 저장된 이미지 경로 설정
//            imageUrl = fileName;  // 예시 URL 경로
//        } else {
//            // 이미지가 첨부되지 않았을 경우 기본 이미지 URL 설정
//            imageUrl = null;  // 기본 이미지 URL
//        }
//        
//        // MemberEntity 생성
//        MemberEntity entity = MemberEntity.builder()
//                .profileImg(imageUrl)  // 이미지 URL 설정
//                .memberId(dto.getMemberId())
//                .memberPw(passwordEncoder.encode(dto.getMemberPw()))        // Encrypt password
//                .memberName(dto.getMemberName())
//                .email(dto.getEmail())
//                .enabled(true) // Account enabled
//                .roleName(dto.getRoleName()) // Set role from DTO
//                .createdTime(now)
//                .updatedTime(now)
//                .build();
//       
//        
//        // Special handling for admin account
//        if (dto.getMemberId().equals("admin123")) {
//        	entity.setRoleName(RoleName.ROLE_ADMIN);
//        }
//
//        return memberRepository.save(entity);
//    }
    
    public MemberEntity join(MemberDTO dto, String uploadPath, MultipartFile profileImg) throws IOException {
        LocalDateTime now = LocalDateTime.now();
        String imageUrl = null;

        if (dto.getProfileImg() != null && !dto.getProfileImg().isEmpty()) {
            File directoryPath = new File(uploadPath);
            if (!directoryPath.exists()) {
                directoryPath.mkdirs();
            }

            String originalName = dto.getProfileImg().getOriginalFilename();
            String extension = originalName.substring(originalName.lastIndexOf("."));
            String dateString = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String uuidString = UUID.randomUUID().toString();
            String fileName = dateString + "_" + uuidString + extension;

            File file = new File(uploadPath, fileName);
            dto.getProfileImg().transferTo(file);
            imageUrl = fileName;
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


    public MemberEntity findByMemberId(String memberId) {
        return memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("User not found with memberId: " + memberId));
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
    
//    public void updateMember(MemberDTO dto, MultipartFile profileImg) throws IOException {
//        MemberEntity member = memberRepository.findById(dto.getMemberId())
//            .orElseThrow(() -> new RuntimeException("Member not found"));
//
//        member.setMemberName(dto.getMemberName());
//        member.setEmail(dto.getEmail());
//        
//        // 비밀번호 처리
//        if (dto.getMemberPw() != null && !dto.getMemberPw().trim().isEmpty()) {
//            // 새 비밀번호가 제공된 경우에만 업데이트
//            member.setMemberPw(passwordEncoder.encode(dto.getMemberPw()));
//        }
//        // 비밀번호 필드가 null이거나 공백인 경우 기존 비밀번호 유지
//
//        if (profileImg != null && !profileImg.isEmpty()) {
//            String imageUrl = saveProfileImage(profileImg);
//            member.setProfileImg(imageUrl);
//        }
//
//        memberRepository.save(member);
//    }
    
    public void updateMember(MemberDTO dto, MultipartFile profileImg, String uploadPath) throws IOException {
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

        memberRepository.save(member);
    }
    
//    private String saveProfileImage(MultipartFile profileImg) throws IOException {
//        // 파일이 비어있는지 확인
//        if (profileImg == null || profileImg.isEmpty()) {
//            throw new IllegalArgumentException("파일이 비어있습니다.");
//        }
//
//        // 원본 파일명 가져오기
//        String originalFilename = profileImg.getOriginalFilename();
//        
//        // 파일 확장자 추출
//        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
//        
//        // 현재 날짜를 문자열로 변환 (예: 20240912)
//        String dateString = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
//        
//        // UUID 생성
//        String uuid = UUID.randomUUID().toString();
//        
//        // 새 파일명 생성 (날짜_UUID.확장자)
//        String newFilename = dateString + "_" + uuid + extension;
//        
//        // 저장할 경로 설정 (예: /path/to/upload/directory/)
//        String uploadDir = "/path/to/upload/directory/";
//        File dir = new File(uploadDir);
//        if (!dir.exists()) {
//            dir.mkdirs();
//        }
//        
//        // 전체 파일 경로
//        String filePath = uploadDir + newFilename;
//        
//        // 파일 저장
//        File dest = new File(filePath);
//        profileImg.transferTo(dest);
//        
//        // 저장된 파일의 상대 경로 또는 URL 반환
//        // 예: /uploads/20240912_abcdef123456.jpg
//        return "/uploads/" + newFilename;
//    }
	
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

        return newFileName;
    }
}
