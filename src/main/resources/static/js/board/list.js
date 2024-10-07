let clientId; // 클라이언트 ID 변수
let freelancerId; // 프리랜서 ID 변수

$(document).ready(function() {
   list();

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
           // 페이지 로드 시 프로젝트 리스트를 불러옴
           list();
       },
       error: function() {
           console.log("비회원");
       }
   });
   
   $('#projectSearchBtn').on('click', function() {
      list();
   });

   $('#projectSearch').on('keypress', function(e) {
       if (e.which === 13) {  // Enter 키 누를 때
           list();
       }
   });
});

function list(sortValue = 'projectCreateDate,desc') {
    const searchTerm = $('#projectSearch').val().trim();
    
    // 기본 프로젝트 리스트 가져오기
    $.ajax({
        url: 'list',
        type: 'get',
        data: { sort: sortValue, word: searchTerm },
        success: function(list) {
            sortList(list, sortValue);
            $('#output').empty();
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
                $('#output').append(html);
            });
        },
        error: function(e) {
            alert('조회 실패');
        }
    });
}

function sortList(list, sortValue) {
    let [sortField, sortDirection] = sortValue.split(',');

    if (sortField === "recommended") {
        // 추천 프로젝트 가져오기
        $.ajax({
            url: 'recommendations/projects',
            method: 'GET',
            data: { freelancerId: freelancerId }, // 프리랜서 ID를 쿼리 파라미터로 전달
            success: function(recommendedList) {
                $('#output').empty();
                $(recommendedList).each(function(i, obj) {
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
                    $('#output').append(html);
                });
            },
            error: function() {
                alert("추천 정보를 가져오는 데 실패했습니다.");
            }
        });
        return; // 추천 요청 후 더 이상 진행하지 않도록 반환
    }

    // 기본 정렬 로직
    list.sort(function(a, b) {
        let fieldA = a[sortField];
        let fieldB = b[sortField];

        if (sortDirection === 'asc') {
            return fieldA > fieldB ? 1 : -1;
        } else {
            return fieldA < fieldB ? 1 : -1;
        }
    });
}

function calculateRemainingDays(StartDate, EndDate) {
    const start = new Date(StartDate); // 시작 날짜
    const end = new Date(EndDate); // 끝날짜
    const differenceInTime = end - start; // 시간 차이 (밀리초)
    const differenceInDays = Math.ceil(differenceInTime / (1000 * 60 * 60 * 24)); // 일로 변환 후 올림
    return differenceInDays >= 0 ? differenceInDays : 0; // 음수일 경우 0일로 표시
}

function formatBudget(budget) {
    return Math.floor(budget).toLocaleString() + '원';
}
