/**
 * 
 */

let freelancers = [];

$(document).ready(function() {
	loadPartners();	
	$('input[type="checkbox"]').on("click", function() {
		loadPartners()
	})
	
	$('#freelancerSearchBtn').on('click', function() {
		loadPartners();
    });

    $('#freelancerSearch').on('keypress', function(e) {
        if (e.which === 13) {  // Enter 키 누를 때
        	loadPartners();
        }
    });
	
	
});

function loadPartners() {
    // 선택된 필터 값들을 수집
    var selectedFields = $('input[name="field"]:checked').map(function() {
        return this.value;
    }).get();
    
    var selectedAreas = $('input[name="area"]:checked').map(function() {
        return this.value;
    }).get();

    // 선택된 값이 없을 경우 기본값 설정
    if (selectedFields.length === 0) {
        selectedFields = $('input[name="field"]').map(function() {
            return this.value;
        }).get();
    }
    
    if (selectedAreas.length === 0) {
        selectedAreas = $('input[name="area"]').map(function() {
            return this.value;
        }).get();
    }
    const searchTerm = $('#freelancerSearch').val().trim();
        

    // AJAX 요청
    $.ajax({
        url: '/find/findFreelancer',
        method: 'GET',
        data: {
            fields: selectedFields,
            areas: selectedAreas,
            search: searchTerm
        },
        dataType: 'json',
        success: function(response) {
            freelancers = response;  // 프리랜서 목록을 전역 변수에 저장
            updatePartnerList(freelancers);  // 기본 목록 업데이트
			$('.partner-card').on('click', function() {
                window.location.href="/find/freelancerDetail?memberId="+ $(this).attr('data');
            });
        },
        error: function(xhr, status, error) {
            console.error("파트너 데이터를 불러오는 데 실패했습니다:", error);
            $('#partnerResults').html('<p>데이터를 불러오는 데 실패했습니다. 다시 시도해 주세요.</p>');
        }
    });
}

function updatePartnerList(response) {
    // 기존 파트너 목록 비우기
    $('#partners').empty();
    
    // 응답이 배열인지 확인
    if (Array.isArray(response)) {
        response.forEach(function(partner) {
            $('#partners').append(createPartnerCard(partner));
			$('.partner-card').on('click', function() {
			                window.location.href="/find/freelancerDetail?memberId="+ $(this).attr('data');
			            });
        });
    } else {
        console.error("예상치 못한 응답 형식:", response);
    }
}

function createPartnerCard(partner) {
	function getStarRating(rating) {
	        const fullStars = Math.floor(rating);
	        const halfStar = rating % 1 >= 0.5 ? 1 : 0;
	        const emptyStars = 5 - fullStars - halfStar;
	        
	        return '★'.repeat(fullStars) + (halfStar ? '½' : '') + '☆'.repeat(emptyStars);
	    }
	
    return `
        <div class="partner-card" data="${partner.memberId}">
            <img src="${partner.profileImg}" alt="${partner.name}" class="partner-image" width="300px" height="300px">
            <div class="partner-info">
                <h3 class="partner-id">${partner.memberId || '아이디 없음'}</h3>
                <div class="rating">${getStarRating(partner.totalRating)} ${partner.totalRating} / 평가 ${partner.totalReviews || 0}개</div>
                <div class="partner-stats">
                    <span>계약한 프로젝트: ${partner.totalProjects || 0}건</span>
                    <span>포트폴리오: ${partner.totalPortfolios || 0}개</span>
                </div>
                <div class="skills">
                    ${partner.skills ? partner.skills.join(', ') : '스킬 정보 없음'}
                </div>
            </div>
        </div>
    `;
}

function sortFreelancers(sortType) {
    let sortedFreelancers = [...freelancers];  // 원본 배열 복사

    if (sortType === "rating") {
        // 평점 높은 순으로 정렬
        sortedFreelancers.sort((a, b) => b.totalRating - a.totalRating);
    } else if (sortType === "portfolio") {
        // 포트폴리오 많은 순으로 정렬
        sortedFreelancers.sort((a, b) => b.totalPortfolios - a.totalPortfolios);
    } else if (sortType === "default") {
        // 기본 정렬: 불러온 데이터를 그대로 사용
        sortedFreelancers = freelancers;  // 원본 데이터를 그대로 사용
    }

    // 정렬된 리스트 다시 표시
    updatePartnerList(sortedFreelancers);
}