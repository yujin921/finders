/**
 * 
 */


$(document).ready(function() {
    function showTab(tabName) {
        // 탭 활성화 상태 변경
        $('.tab').removeClass('active');
        $(`.tab[data-tab="${tabName}"]`).addClass('active');

        // 탭 제목 변경
        $('#tabTitle').text(`${tabName} 프로젝트`);

        // 로딩 메시지 표시
        $('#projectList').html('<li class="loading">프로젝트를 불러오는 중...</li>');

        // AJAX를 사용하여 프로젝트 데이터 가져오기
        $.ajax({
            url: `/project/projectList`,
            method: 'GET',
            data: { status: tabName },
            dataType: 'json',
            success: function(projects) {
                var projectListHtml = '';
                $.each(projects, function(index, project) {
                    projectListHtml += `<li class="project-item">
											<a href="/myProject/management?projectNum=${project.projectNum}">
											${index + 1}. ${project.projectName}
											</a>
										</li>`;
                });
                $('#projectList').html(projectListHtml);
            },
            error: function(xhr, status, error) {
                console.error('Error fetching projects:', error);
                $('#projectList').html('<li class="loading">프로젝트를 불러오는데 실패했습니다.</li>');
            }
        });
    }

    // 탭 클릭 이벤트 처리
    $('.tab').on('click', function() {
        showTab($(this).data('tab'));
    });
	const tab = $('.main-content').data('status')
    // 초기 탭 설정
	if(tab == '모집중') {
		showTab('모집중');
	} else if(tab == '진행중') {
		showTab('진행중');	
	} else if(tab == '완료된') {
		showTab('완료된');	
	}
});