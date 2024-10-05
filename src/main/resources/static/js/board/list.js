$(document).ready(function() {
   list();
   $('#projectSearchBtn').on('click', function() {
	list();
   });

   $('#projectSearch').on('keypress', function(e) {
       if (e.which === 13) {  // Enter 키 누를 때
       	list();
       }
   });
});

function list(sortValue = 'projectCreateDate,asc') {
	
	const searchTerm = $('#projectSearch').val().trim();
	
    $.ajax({
        url: 'list',
        type: 'get',
      data: { sort: sortValue
		, word: searchTerm
	   },
        success: function(list) {
         sortList(list, sortValue);
            $('#output').empty();
         $(list).each(function(i, obj) {
            let status = obj.projectStatus ? "모집완료" : "모집중";
            let remainingDays = calculateRemainingDays(obj.projectStartDate, obj.projectEndDate);

            let html = `
               <div class="post" onclick="location.href='/board/read?projectNum=${obj.projectNum}';">
                  <img src="data:image/jpeg;base64,${obj.projectImage}" alt="Project Image" class="thumbnail">
                  <div class="post-info">
                     <span class="title">${obj.projectTitle}</span>
                     <span class="rating">평점: ${obj.averageRating}점</span><br>
                     <span class="startdate">시작일 : ${obj.projectStartDate}</span><br>
                     <span class="budget">금액 : ${obj.projectBudget}</span>
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

function calculateRemainingDays(StartDate, EndDate) {
   const start = new Date(StartDate); // 시작 날짜
   const end = new Date(EndDate); // 끝날짜
   const differenceInTime = end - start; // 시간 차이 (밀리초)
   const differenceInDays = Math.ceil(differenceInTime / (1000 * 60 * 60 * 24)); // 일로 변환 후 올림
   return differenceInDays >= 0 ? differenceInDays : 0; // 음수일 경우 0일로 표시
}

function sortList(list, sortValue) {
   let [sortField, sortDirection] = sortValue.split(',');

   if (sortField === "recommend") {
      // 추천순 기능이 추가되면 여기에 로직 구현
      return;
   }

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