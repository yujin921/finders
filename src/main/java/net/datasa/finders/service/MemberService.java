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
//    	LocalDateTime now = LocalDateTime.now();  // 회원가입 일시, 회원정보 수정 일시
//    	
//    	// 첨부파일이 있는지 확인
//    	if (profileImg != null && !profileImg.isEmpty()) {
//    		// 저장할 경로의 디렉토리가 있는지 확인 -> 없으면 생성
//    		File directoryPath = new File(uploadPath);
//    		
//    		if (!directoryPath.isDirectory()) {
//    			directoryPath.mkdirs();
//    		}
//    	}
//    				
//    	// 저장할 파일명 생성
//    	// 내 이력서.doc -> 20240806_6156df53-a49b-4419-a336-cb6da0fa9640.doc
//    	String originalName = profileImg.getOriginalFilename();
//    	String extension = originalName.substring(originalName.lastIndexOf("."));  // 확장자를 가져옴
//    	String dateString = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
//    	String uuidString = UUID.randomUUID().toString();
//    	String fileName = dateString + "_" + uuidString + extension;
//    	
//    	File file = new File(uploadPath, fileName);
//    	profileImg.transferTo(file);
//    	
//    	MemberEntity entity = MemberEntity.builder()
//    			.profileImg(fileName)
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
//        // Special handling for admin account
//        if (dto.getMemberId().equals("admin123")) {
//            entity = MemberEntity.builder()
//            		.profileImg(fileName)
//                    .memberId(dto.getMemberId())
//                    .memberPw(passwordEncoder.encode(dto.getMemberPw()))        // Encrypt password
//                    .memberName(dto.getMemberName())
//                    .email(dto.getEmail())
//                    .enabled(true) // Account enabled
//                    .roleName(RoleName.ROLE_ADMIN) // Set role to ADMIN
//                    .createdTime(now)
//                    .updatedTime(now)
//                    .build();
//        }
//
//        return memberRepository.save(entity);
//    }
    
    public MemberEntity join(MemberDTO dto, String uploadPath, MultipartFile profileImg) throws IOException {
        
        LocalDateTime now = LocalDateTime.now();  // 회원가입 일시, 회원정보 수정 일시
        
        String imageUrl;
        
        // 첨부파일이 있는지 확인
        if (profileImg != null && !profileImg.isEmpty()) {
            // 저장할 경로의 디렉토리가 있는지 확인 -> 없으면 생성
            File directoryPath = new File(uploadPath);
            
            if (!directoryPath.isDirectory()) {
                directoryPath.mkdirs();
            }
            
            // 저장할 파일명 생성
            String originalName = profileImg.getOriginalFilename();
            String extension = originalName.substring(originalName.lastIndexOf("."));  // 확장자를 가져옴
            String dateString = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String uuidString = UUID.randomUUID().toString();
            String fileName = dateString + "_" + uuidString + extension;
            
            File file = new File(uploadPath, fileName);
            profileImg.transferTo(file);
            
            // 이미지가 업로드된 경우 서버 내 저장된 이미지 경로 설정
            imageUrl = fileName;  // 예시 URL 경로
        } else {
            // 이미지가 첨부되지 않았을 경우 기본 이미지 URL 설정
            imageUrl = null;  // 기본 이미지 URL
        }
        
        // MemberEntity 생성
        MemberEntity entity = MemberEntity.builder()
                .profileImg(imageUrl)  // 이미지 URL 설정
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
	
}
