$(document).ready(function () {
	let clientId; // 클라이언트 ID 변수
	let freelancerId; // 프리랜서 ID 변수

	// 로그인한 사용자의 ID를 가져오는 AJAX 요청
	$.ajax({
	    url: 'currentUser', // 현재 로그인한 사용자 정보를 가져오는 API
	    method: 'GET',
	    success: function(data) {
	        // RoleName에 따라 ID 할당
	        if (data.roleName === 'ROLE_FREELANCER') {
	            freelancerId = data.freelancerId; // 프리랜서 ID 할당
	        } else if (data.roleName === 'ROLE_CLIENT') {
	            clientId = data.clientId; // 클라이언트 ID 할당
	        }
	    },
	    error: function() {
	        console.log("비회원");
	    }
	});

	// 프로젝트 찾기 페이지에서 정렬 옵션 변경 시 호출
	window.list = function (sortValue) {
	    if (sortValue === 'recommended') {
	        // 추천순을 선택했을 때 AJAX 요청 보내기
	        $.ajax({
	            url: 'recommendations/projects', // 추천 프로젝트를 가져오는 API 호출
	            method: 'GET',
	            data: { freelancerId: freelancerId }, // 프리랜서 ID를 쿼리 파라미터로 전달
	            success: function(list) {
	                $('#output').empty(); // 기존 출력 내용 비우기
	                $(list).each(function(i, obj) {
	                    let status = obj.projectStatus ? "모집완료" : "모집중";
	                    let remainingDays = calculateRemainingDays(obj.projectStartDate, obj.projectEndDate);
	                    const formattedBudget = formatBudget(obj.projectBudget);
	                    
	                    let html = `
	                        <div class="post" onclick="location.href='/board/read?projectNum=${obj.projectNum}';">
	                            <img src="data:image/jpeg;base64,${obj.projectImage}" alt="Project Image" class="thumbnail">
	                            <div class="post-info">
	                                <span class="title">${obj.projectTitle}</span>
	                                <span class="rating">평점: ${obj.averageRating}점</span><br>
	                                <span class="startdate">시작일 : ${obj.projectStartDate}</span><br>
	                                <span class="budget">금액 : ${formattedBudget}</span>
	                                <span class="author">작성자 : ${obj.clientId}</span>
	                                <span class="date">예상 기간 : ${remainingDays}일</span>
	                                <span class="status">${status}</span>
	                            </div>
	                        </div>
	                    `;
						
	                    $('#output').append(html); // HTML 추가
	                });
	            },
	            error: function() {
	                alert("추천 정보를 가져오는 데 실패했습니다.");
	            }
	        });
	    } 
	};
	
    // 프리랜서 찾기 페이지에서 정렬 옵션 변경 시 호출
	window.sortFreelancers = function (sortValue) {
	    if (sortValue === 'recommended') {
	        // 추천순을 선택했을 때 AJAX 요청 보내기
	        $.ajax({
	            url: 'recommendations/freelancers?clientId=' + clientId, // 클라이언트 ID를 쿼리 파라미터로 전송
	            method: 'GET',
	            success: function(data) {
	                $('#partners').empty(); // 기존 출력 내용 비우기
	                $(data).each(function(i, partner) {
	                    function getStarRating(rating) {
	                        const fullStars = Math.floor(rating);
	                        const halfStar = rating % 1 >= 0.5 ? 1 : 0;
	                        const emptyStars = 5 - fullStars - halfStar;

	                        return (
	                            '<i class="fas fa-star"></i>'.repeat(fullStars) +
	                            (halfStar ? '<i class="fas fa-star-half-alt"></i>' : '') +
	                            '<i class="far fa-star"></i>'.repeat(emptyStars)
	                        );
	                    }

	                    let html = `
	                        <div class="partner-card" data="${partner.memberId}">
	                            <div class="image-container">
	                                <img src="${partner.profileImg}" alt="${partner.name}" class="partner-image">
	                            </div>
	                            <div class="partner-info">
	                                <h3 class="partner-id">${partner.memberId || '아이디 없음'}</h3>
	                                <div class="rating">
	                                    ${getStarRating(partner.totalRating)} 
	                                    <span class="rating-number">${partner.totalRating.toFixed(2)} / 평가 ${partner.totalReviews || 0}개</span>
	                                </div>
	                                <div class="partner-stats">
	                                    <div class="stat-item">
	                                        <i class="fas fa-briefcase"></i>
	                                        <span>계약한 프로젝트: ${partner.totalProjects || 0}건</span>
	                                    </div>
	                                    <div class="stat-item">
	                                        <i class="fas fa-folder-open"></i>
	                                        <span>포트폴리오: ${partner.totalPortfolios || 0}개</span>
	                                    </div>
	                                </div>
	                                <div class="skills">
	                                    ${partner.skills ? partner.skills.map(skill => `<span class="skill">${skill}</span>`).join('') : '<span class="no-skills">스킬 정보 없음</span>'}
	                                </div>
	                            </div>
	                        </div>
	                    `;
	                    
	                    $('#partners').append(html); // HTML 추가
	                });
					
					// 클릭 이벤트 리스너 추가
	                $('.partner-card').on('click', function() {
	                    window.location.href = "/find/freelancerDetail?memberId=" + $(this).attr('data');
	                });
	            },
	            error: function() {
	                alert("추천 정보를 가져오는 데 실패했습니다.");
	            }
	        });
	    } 
	};
});
