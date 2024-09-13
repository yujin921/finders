$(document).ready(function() {
			list();
		});

		function list() {
		    $.ajax({
		        url: 'list',
		        type: 'get',
		        success: function(list) {
		            $('#output').empty();

					$(list).each(function(i, obj) {
						const formattedDate = new Date(obj.recruitDeadline).toISOString().split('T')[0];
						let status = obj.projectStatus ? "모집완료" : "모집중";

						let remainingDays = calculateRemainingDays(obj.projectStartDate, obj.projectEndDate);

						let html = `
							<div class="post">
								<img src="data:image/jpeg;base64,${obj.projectImage}" alt="Project Image" class="thumbnail">
								<div class="post-info">
									<a href="/board/read?projectNum=${obj.projectNum}">${obj.projectTitle}</a>
									<span>평점들어갈 자리</span><br>
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