let freelancers = [];
let clientId;
let currentPage = 0;
const itemsPerPage = 10; // 페이지당 표시할 항목 수

$(document).ready(function() {
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

    loadPartners(currentPage);
    
    // 페이지네이션 버튼 이벤트 리스너 추가
    $(document).on('click', '.pagination .page-link', function(e) {
        e.preventDefault();
        const page = $(this).data('page');
        loadPartners(page);
    });
});

function loadPartners(page = 0) {
    let selectedFields = $('input[name="field"]:checked').map(function() { return this.value; }).get();
    let selectedAreas = $('input[name="area"]:checked').map(function() { return this.value; }).get();
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

    $.ajax({
        url: '/find/findFreelancer',
        method: 'GET',
        data: {
            fields: selectedFields,
            areas: selectedAreas,
            search: searchTerm,
            page: page,
            size: itemsPerPage
        },
        dataType: 'json',
        success: function(response) {
            freelancers = response.content;  // 페이지 내용
            updatePartnerList(freelancers);
            updatePagination(response);  // 페이지네이션 업데이트
        },
        error: function(xhr, status, error) {
            console.error("파트너 데이터를 불러오는 데 실패했습니다:", error);
            $('#partnerResults').html('<p>데이터를 불러오는 데 실패했습니다. 다시 시도해 주세요.</p>');
        }
    });
}

function updatePagination(pageData) {
    const totalPages = pageData.totalPages;
    currentPage = pageData.number;

    let paginationHtml = '<ul class="pagination justify-content-center">';

    // 이전 페이지 버튼
    if (currentPage > 0) {
        paginationHtml += `<li class="page-item"><a class="page-link" href="#" data-page="${currentPage - 1}">이전</a></li>`;
    }

    // 페이지 번호
    for (let i = 0; i < totalPages; i++) {
        if (i === currentPage) {
            paginationHtml += `<li class="page-item active"><span class="page-link">${i + 1}</span></li>`;
        } else {
            paginationHtml += `<li class="page-item"><a class="page-link" href="#" data-page="${i}">${i + 1}</a></li>`;
        }
    }

    // 다음 페이지 버튼
    if (currentPage < totalPages - 1) {
        paginationHtml += `<li class="page-item"><a class="page-link" href="#" data-page="${currentPage + 1}">다음</a></li>`;
    }

    paginationHtml += '</ul>';

    $('#pagination').html(paginationHtml);
}

function updatePartnerList(partners) {
    $('#partners').empty();
    partners.forEach(function(partner) {
        $('#partners').append(createPartnerCard(partner));
    });
    $('.partner-card').on('click', function() {
        window.location.href = "/find/freelancerDetail?memberId=" + $(this).attr('data');
    });
}

function sortFreelancers(sortType) {
    if (sortType === "recommended") {
        $.ajax({
            url: 'recommendations/freelancers',
            method: 'GET',
            data: { clientId: clientId, page: currentPage, size: itemsPerPage },
            success: function(response) {
                updatePartnerList(response.content);
                updatePagination(response);
            },
            error: function() {
                alert("추천 정보를 가져오는 데 실패했습니다.");
            }
        });
    } else {
        // 클라이언트 측 정렬 로직
        let sortedFreelancers = [...freelancers];
        if (sortType === "rating") {
            sortedFreelancers.sort((a, b) => b.totalRating - a.totalRating);
        } else if (sortType === "portfolio") {
            sortedFreelancers.sort((a, b) => b.totalPortfolios - a.totalPortfolios);
        }
        updatePartnerList(sortedFreelancers);
    }
}

function createPartnerCard(partner) {
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

    return `
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
}