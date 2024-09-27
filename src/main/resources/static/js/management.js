window.addEventListener('load', adjustSidebarHeight);
window.addEventListener('resize', adjustSidebarHeight);

function adjustSidebarHeight() {
    const container = document.querySelector('.container');
    const sidebar = document.querySelector('.sidebar');
    const mainContent = document.querySelector('.main-content');

    // 사이드바의 높이를 전체 컨테이너 높이에서 main-content의 높이를 뺀 값으로 설정
    sidebar.style.height = `${container.clientHeight}px`;
}

// 간트차트 진행도 표시 함수
function updateProgressDisplay() {
	// 스크롤바에서 현재 값 가져오기
	const progressValue = document.getElementById('progress-value').value;
	
	// 해당 값을 표시하는 요소 업데이트
	document.getElementById('progress-value-display').textContent = `${progressValue}%`;
}

document.addEventListener('DOMContentLoaded', function() {
    const tabs = document.querySelectorAll('.tab-link');
    const contents = document.querySelectorAll('.tab-content');
	let calendar;
    const calendarEl = document.getElementById('calendar');
	let selectedDate; // 선택한 날짜 저장 변수
	let ganttChart; // Gantt 차트 인스턴스를 저장할 변수
	let timeline;
	let ganttChartLoaded = false;
	
	// 사용자 정보를 담은 객체 생성
    const userData = {
        role: document.getElementById('user-data').dataset.role,
        id: document.getElementById('user-data').dataset.id
    };

    console.log("User Data:", userData);
    console.log("User Role:", userData.role);
    console.log("User ID:", userData.id);
	
	// URL에서 쿼리 파라미터를 추출하는 함수
    function getQueryParam(param) {
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get(param);
    }

    // 페이지 로드 시 projectNum 값을 추출하여 input 태그에 설정
    const projectNum = getQueryParam('projectNum');
    if (projectNum) {
        $('#project-num').val(projectNum);
    }

    console.log("projectNum 체크용: ", projectNum);

	// 페이지 로드 시 기본적으로 업무 목록 로드
	loadTasks();
	
    tabs.forEach(tab => {
        tab.addEventListener('click', function(event) {
            event.preventDefault();
            const targetId = this.getAttribute('data-tab');

            contents.forEach(content => {
                content.classList.remove('active');
            });

            document.getElementById(targetId).classList.add('active');

            if (targetId === 'task-content') {
				// 페이지 로드 시 업무 목록을 불러옴
				loadTasks();
			} else if (targetId === 'calendar-content') {
                loadCalendar();
            } else if (targetId === 'gantt-content') {
				importGanttChartData();
            } else if (targetId === 'application-content') {
                loadApplicationList();
            } else if (targetId === 'team-content') {
				loadTeamList();
			}
        });
    });

	/*
    // 업무 등록 버튼 이벤트 리스너
    const addTaskButton = document.getElementById('add-task-button');
    if (addTaskButton) {
        addTaskButton.addEventListener('click', openTaskModal);
    }

    // 업무 모달 열기
    function openTaskModal() {
        const modal = document.createElement('div');
        modal.classList.add('modal');

        const modalContent = document.createElement('div');
        modalContent.classList.add('modal-content');

        const title = document.createElement('h3');
        title.textContent = '업무 등록';

        const form = document.createElement('form');
        form.innerHTML = `
            <div class="form-group">
                <label for="task-name">업무명</label>
                <input type="text" id="task-name" required>
            </div>
            
            <div class="form-group">
                <label for="task-status">상태</label>
                <select id="task-status" required>
                    <option value="요청">요청</option>
                    <option value="진행">진행</option>
                    <option value="피드백">피드백</option>
                    <option value="보류">보류</option>
                    <option value="완료">완료</option>
                </select>
            </div>
            
            <div class="form-group">
                <label for="task-priority">우선순위</label>
                <select id="task-priority" required>
                    <option value="낮음">낮음</option>
                    <option value="보통">보통</option>
                    <option value="높음">높음</option>
                    <option value="긴급">긴급</option>
                </select>
            </div>

            <div class="form-group">
                <label for="task-assignee">담당자</label>
                <input type="text" id="task-assignee" required>
            </div>

            <div class="modal-buttons">
                <button type="button" class="btn-close">취소</button>
                <button type="submit" class="btn-save">저장</button>
            </div>
        `;

        modalContent.appendChild(title);
        modalContent.appendChild(form);
        modal.appendChild(modalContent);

        document.body.appendChild(modal);

        const closeButton = modal.querySelector('.btn-close');
        const saveButton = modal.querySelector('.btn-save');
        
        closeButton.addEventListener('click', () => {
            document.body.removeChild(modal);
        });

        form.addEventListener('submit', function(event) {
            event.preventDefault();
            const name = document.getElementById('task-name').value;
            const status = document.getElementById('task-status').value;
            const priority = document.getElementById('task-priority').value;
            const assignee = document.getElementById('task-assignee').value;

            addTaskToTable(name, status, priority, assignee);
            document.body.removeChild(modal);
        });
    }

    // 업무를 테이블에 추가하는 함수
    function addTaskToTable(name, status, priority, assignee) {
        const taskTableBody = document.querySelector('#task-table tbody');
        const newRow = document.createElement('tr');

        newRow.innerHTML = `
            <td class="task-name">${name}</td>
            <td>${status}</td>
            <td>${priority}</td>
            <td>${assignee}</td>
        `;

        // 상태가 '완료'일 때 가운데 밑줄 추가
        if (status === '완료') {
            newRow.querySelector('.task-name').style.textDecoration = 'line-through';
            newRow.querySelector('.task-name').style.textDecorationColor = 'red'; // 선택적으로 색상 변경 가능
        }

        // 상태가 변경될 때 자동으로 가운데 밑줄 업데이트
        newRow.querySelector('td:nth-child(2)').addEventListener('change', function() {
            if (this.textContent === '완료') {
                newRow.querySelector('.task-name').style.textDecoration = 'line-through';
            } else {
                newRow.querySelector('.task-name').style.textDecoration = 'none';
            }
        });

        taskTableBody.appendChild(newRow);
    }
	*/

    // 업무 등록 버튼 클릭 시 모달 창 열기
    $('.add-task-buttons').on('click', function() {
		
		console.log('Opening task modal'); // 모달이 열릴 때 로그 출력
		console.log('Project Number:', projectNum); // projectNum 값 로그 출력
		
        $('#task-modal').removeClass('hidden').show();
        $('#task-form')[0].reset(); // 폼 초기화

        // 첫 번째 업무 등록 여부 확인
        const isFirstTask = !localStorage.getItem('firstTaskRegistered');

        // 기능 분류 목록 로드
        loadFunctionTitles(projectNum, isFirstTask);

        // 첫 번째 업무 등록 이후 상태 저장
        if (isFirstTask) {
            localStorage.setItem('firstTaskRegistered', 'true');
        }
		
		// 자동완성 초기화 호출
		initializeAssigneeAutocomplete();
		
    });

    // 모달 닫기 버튼 클릭 시
    $('#close-task-modal').on('click', function() {
        $('#task-modal').addClass('hidden').hide();
    });

	// 모달 바깥 영역 클릭 시 모달 닫기
	$('#task-modal').on('click', function(event) {
		if (event.target === this) { // 모달 바깥 영역(오버레이)을 클릭했을 때만 닫기
			$(this).addClass('hidden').hide();
		}
	});

    console.log("projectNum 체크용: ", projectNum);

    // 기능 분류 목록 로드 함수
    function loadFunctionTitles(projectNum, isFirstTask) {
    	console.log('Loading function titles for projectNum:', projectNum); // 디버깅을 위한 로그
    	console.log("isFirstTask 체크용: ", isFirstTask);
    	
        $.ajax({
            url: 'loadFunctionTitles',
            type: 'get',
            data: { projectNum: projectNum },
            success: function(response) {
                const $select = $('#function-select');
                $select.empty().append('<option value="">기존 기능 선택</option>');
                response.forEach(function(item) {
                    $select.append(`<option value="${item.functionTitleId}">${item.titleName}</option>`);
                });

                if (isFirstTask) {
                    // 첫 번째 업무 등록 시
                    $('#function-select').hide();
                    $('#new-function-name').show();
                    $('#add-new-function-btn').show();
                } else {
                    // 두 번째 이후 업무 등록 시
                    $('#function-select').show();
                    $('#new-function-name').show();
                    $('#add-new-function-btn').show();
                }
            },
            error: function() {
                alert('기능 분류 목록을 불러오는 데 실패했습니다.');
            }
        });
    }

    // 기능 분류 선택에 따라 새 기능 입력 필드 표시/숨기기
    $('#function-select').on('change', function() {
        if ($(this).val() === '') {
            $('#new-function-name').show();
            $('#add-new-function-btn').show(); // 새 기능 추가 버튼 표시
        } else {
            $('#new-function-name').hide();
            $('#add-new-function-btn').hide(); // 새 기능 추가 버튼 숨기기
        }
        // 선택한 기능 분류 ID를 전역 변수에 저장
        funcTitleId = $(this).val();
    });
	
	let assignees = []; // AJAX로 불러온 데이터 저장할 배열

	// "담당자" 입력란에 ID 입력 시 해당 프로젝트에 참여하는 프리랜서 ID 자동완성
	function initializeAssigneeAutocomplete() {
	    console.log('Initializing assignee autocomplete'); // 로그 추가

	    console.log("User Data 체크용:", userData); // 확인용 로그
	    console.log("User Role 체크용:", userData.role);
	    console.log("User ID 체크용:", userData.id);

	    // 담당자 입력란 초기화
	    if (userData.role === 'ROLE_FREELANCER' && userData.id) {
	        $('#task-assignee').val(userData.id); // 자신의 ID 설정
	        $('#task-assignee').prop('readonly', true); // 읽기 전용으로 설정
	        $('#task-assignee').css('background-color', '#f0f0f0'); // 배경색 변경
	    } else {
	        $('#task-assignee').prop('readonly', false); // 일반 사용자 경우 수정 가능
	    }

	    // 프리랜서 데이터 불러오기
	    loadFreelancers();

	    // 자동완성 설정
	    $('#task-assignee').autocomplete({
	        source: assignees.map(f => f.id), // 초기 데이터로 자동완성 목록 제공
	        minLength: 1,
	        select: function(event, ui) {
	            console.log("Selected value:", ui.item.value);
	            $('#task-assignee').val(ui.item.value); // 선택된 값 설정
	        }
	    });

	    // 입력 필드 이벤트 핸들링
	    $('#task-assignee').on('input', function() {
	        let value = $(this).val().toLowerCase();
	        $('#autocomplete-list').empty(); // 이전 목록 비우기

	        if (value) {
	            let filteredAssignees = assignees.filter(assignee => assignee.id.toLowerCase().includes(value));
	            console.log('Filtered Assignees:', filteredAssignees); // 필터링된 결과 로그

	            filteredAssignees.forEach(assignee => {
	                $('#autocomplete-list').append(`<div class="autocomplete-item">${assignee.id}</div>`);
	            });
	            $('#autocomplete-list').removeClass('hidden'); // 목록 보이기
	        } else {
	            $('#autocomplete-list').addClass('hidden'); // 입력이 없으면 목록 숨기기
	        }
	    });

	    // 클릭 시 입력 필드에 값 설정
	    $(document).on('click', '.autocomplete-item', function() {
	        $('#task-assignee').val($(this).text());
	        $('#autocomplete-list').addClass('hidden'); // 목록 숨기기
	    });

	    // 입력 필드 밖 클릭 시 목록 숨기기
	    $(document).on('click', function(e) {
	        if (!$(e.target).closest('#task-assignee').length) {
	            $('#autocomplete-list').addClass('hidden');
	        }
	    });

	    console.log("Autocomplete initialized");
	}

	// 프리랜서 데이터를 불러오는 함수
	function loadFreelancers() {
	    $.ajax({
	        url: 'freelancersInput?projectNum=' + projectNum,
	        type: 'GET',
	        dataType: 'json',
	        success: function(data) {
	            console.log("AJAX success, data:", data);
	            // 프리랜서만 필터링
	            assignees = data.filter(freelancer => freelancer.roleName === 'ROLE_FREELANCER')
	                             .map(freelancer => ({
	                id: freelancer.memberId,
	                role: freelancer.roleName
	            }));
	        },
	        error: function() {
	            console.error("AJAX error");
	        }
	    });
	}

	// 폼 제출 이벤트 핸들러
	$('#save-function-and-task-btn').on('click', function() {
	    const newFunctionName = $('#new-function-name').val();
	    const projectNum = $('#project-num').val(); // 프로젝트 번호 값 읽기
	    const selectedFunctionId = $('#function-select').val(); // 선택된 기능 ID

	    if (newFunctionName.trim() === '' && selectedFunctionId === '') {
	        alert('기능 이름을 입력해 주세요 또는 기능을 선택해 주세요.');
	        return;
	    }
	    
	    // functionTitleName을 새 기능 입력값이나 선택된 기능 이름으로 설정
	    const functionTitleName = newFunctionName.trim() !== '' ? newFunctionName : $('#function-select option:selected').text();

	    // 시작 및 종료 날짜를 UTC로 변환
	    const taskStartDate = new Date($('#task-start-date').val());
	    const taskEndDate = new Date($('#task-end-date').val());

	    const formattedStartDate = new Date(taskStartDate.getTime() - (taskStartDate.getTimezoneOffset() * 60000)).toISOString().split('.')[0] + 'Z';
	    const formattedEndDate = new Date(taskEndDate.getTime() - (taskEndDate.getTimezoneOffset() * 60000)).toISOString().split('.')[0] + 'Z';

	    const taskData = {
	        functionTitleId: selectedFunctionId || null, // 선택된 기능 ID, 새 기능의 경우 null 처리
	        functionTitleName: functionTitleName, // 기능 제목 추가
	        taskTitle: $('#task-title').val(),
	        taskDescription: $('#task-description').val(),
	        taskStatus: $('#task-status').val().toUpperCase(), // Enum을 위해 대문자로 변환
	        taskPriority: $('#task-priority').val().toUpperCase(), // Enum을 위해 대문자로 변환
	        taskStartDate: formattedStartDate, // 변환된 시작 날짜
	        taskEndDate: formattedEndDate, // 변환된 종료 날짜
	        freelancerId: $('#task-assignee').val()
	    };

	    console.log('taskData 체크용: ', taskData);

	    $.ajax({
	        url: 'saveFunctionAndTask?projectNum=' + projectNum,
	        type: 'post',
	        data: JSON.stringify(taskData),
	        contentType: 'application/json',
	        dataType: 'json',
	        success: function(response) {
	            console.log('서버 응답:', response);

	            // 새 기능 추가 후 기능 분류 목록 갱신
	            if (newFunctionName.trim() !== '') {
	                $('#function-select').append(`<option value="${response.functionTitleId}">${newFunctionName}</option>`);
	                $('#function-select').val(response.functionTitleId);
	                $('#new-function-name').hide();
	                $('#add-new-function-btn').hide();
	            }

	            alert('업무가 성공적으로 등록되었습니다.');
	            $('#task-modal').addClass('hidden').hide();

	            // 업무 목록 업데이트
	            loadTasks();
				
				// 캘린더 새로 고침(새로 등록한 업무 일정을 캘린더 화면에 반영하기 위해 호출함)
				loadCalendar(); // 캘린더 새로 고침 호출

	        },
	        error: function(xhr) {
	            alert('업무 등록에 실패했습니다: ' + xhr.responseText);
	        }
	    });
	});
    
	function loadTasks() {
	    $.ajax({
	        url: 'getTasks',
	        type: 'get',
	        data: { projectNum: projectNum },
	        success: function(response) {
	            console.log('업무 목록:', response);

	            const $taskList = $('#task-list');
	            $taskList.empty();

	            if (response.length === 0) {
	                $taskList.append('<p>업무가 없습니다.</p>');
	            } else {
	                const uniqueFunctionTitles = [...new Set(response.map(task => task.functionTitleName))];

	                uniqueFunctionTitles.forEach(title => {
	                    const filteredTasks = response.filter(task => task.functionTitleName === title);

	                    if (filteredTasks.length > 0) {
	                        const dropdownContainer = $('<div class="dropdown-container"></div>');
	                        const dropdownToggle = $(`<button class="dropdown-toggle">${title}</button>`);
	                        const dropdownContent = $('<div class="dropdown-content"></div>');

	                        const table = $('<table class="task-table"></table>');
	                        const thead = $(`<thead>
	                            <tr>
	                                <th>업무 제목</th>
	                                <th>설명</th>
	                                <th>상태</th>
	                                <th>우선순위</th>
	                                <th>시작 날짜</th>
	                                <th>종료 날짜</th>
	                                <th>프리랜서 ID</th>
	                            </tr>
	                        </thead>`);
	                        const tbody = $('<tbody></tbody>');

	                        filteredTasks.forEach(task => {
	                            const priorityClass = task.taskPriority.toLowerCase();
	                            const formattedStartDate = formatDateTime(task.taskStartDate);
	                            const formattedEndDate = formatDateTime(task.taskEndDate);

	                            tbody.append(`
	                                <tr>
	                                    <td>${task.taskTitle}</td>
	                                    <td>${task.taskDescription}</td>
	                                    <td>${task.taskStatus}</td>
	                                    <td class="priority-${priorityClass}">${task.taskPriority}</td>
	                                    <td>${formattedStartDate}</td>
	                                    <td>${formattedEndDate}</td>
	                                    <td>${task.freelancerId}</td>
	                                </tr>
	                            `);
	                        });

	                        table.append(thead).append(tbody);
	                        dropdownContent.append(table);
	                        dropdownContainer.append(dropdownToggle).append(dropdownContent);
	                        $taskList.append(dropdownContainer);
	                    }
	                });

	                // 드롭다운 토글 기능
	                $('.dropdown-toggle').on('click', function() {
	                    const $container = $(this).parent('.dropdown-container');
	                    $container.toggleClass('open');  // 클래스 추가로 드롭다운 토글
	                });
	            }
	        },
	        error: function() {
	            alert('업무 목록을 불러오는 데 실패했습니다.');
	        }
	    });
	}

	function formatDateTime(dateString) {
	    const date = new Date(dateString);
	    const year = date.getFullYear();
	    const month = String(date.getMonth() + 1).padStart(2, '0');
	    const day = String(date.getDate()).padStart(2, '0');

	    // 시간, 분, 초 추가
	    const hours = String(date.getHours()).padStart(2, '0');
	    const minutes = String(date.getMinutes()).padStart(2, '0');

	    return `${year}년 ${month}월 ${day}일 ${hours}시 ${minutes}분`;
	}

 	
	// 프로젝트 완료 버튼 클릭 시
	$('#project-completion-button').on('click', function() {
	    
	    if (!projectNum) {
	        alert('프로젝트 번호를 찾을 수 없습니다.');
	        return;
	    }
	    
	    console.log('프로젝트 완료 - projectNum 체크용: ', projectNum);
	    
	    $.ajax({
	        url: 'completeProject?projectNum=' + projectNum, // URL 쿼리 파라미터로 전송
	        type: 'post',
	        dataType: 'text', // 서버에서 반환하는 데이터 형식
	        success: function(response) {
	            if (response === 'success') {
	                alert('프로젝트가 완료되었습니다.');
	                
	                // 목록 화면 업데이트
	                loadTasks();
	            } else {
	                alert('프로젝트 완료 처리에 실패했습니다: ' + response);
	            }
	        },
	        error: function(xhr, status, error) {
	            console.error('AJAX Error:', status, error);
	            alert('서버 요청 중 오류가 발생했습니다.');
	        }
	    });
	});
	
	
	// 캘린더 로드
	function loadCalendar() {
	    if (calendar) {
	        calendar.destroy(); // 기존 캘린더 제거
	    }

	    if (!calendarEl) return; // 캘린더 엘리먼트가 없으면 함수를 종료

	    // 먼저 업무 일정을 가져옵니다.
	    $.ajax({
	        url: `calendar?projectNum=${projectNum}`, // 프로젝트 번호를 쿼리 파라미터로 전달
	        type: 'GET',
	        dataType: 'json',
	        success: function(tasks) {
	            // 캘린더 초기화
	            calendar = new FullCalendar.Calendar(calendarEl, {
	                initialView: 'dayGridMonth', // 초기 뷰 설정
	                headerToolbar: {
	                    left: 'prev,next today', // 이전, 다음, 오늘 버튼
	                    center: 'title', // 제목 중앙 배치
	                    right: 'dayGridMonth,timeGridWeek,timeGridDay,listMonth' // 다양한 뷰 옵션
	                },
	                events: [
	                    ...tasks.map(task => ({
	                        title: task.taskTitle, // 업무 제목
	                        start: task.actualStartDate ? task.actualStartDate : task.taskStartDate, // 실제 시작 날짜가 있으면 사용
	                        end: task.actualEndDate ? task.actualEndDate : task.taskEndDate || undefined, // 실제 종료 날짜가 있으면 사용
	                        color: getColorByStatus(task.taskStatus), // 상태에 따라 색상 변경
	                        extendedProps: {
	                            taskId: task.taskId // 추가 속성으로 taskId 포함
	                        }
	                    }))
	                ],
	                dateClick: function(info) {
	                    // 날짜 클릭 시 선택 모달 열기
	                    document.getElementById('select-modal').classList.remove('hidden');
	                    selectedDate = info.dateStr; // 선택한 날짜 저장
	                },
	                eventClick: function(info) {
	                    // 클릭한 이벤트 정보를 모달로 전달하여 열기
	                    openEventDetailModal(info.event); // FullCalendar 이벤트 객체 전달
	                }
	            });

	            // 이제 업무 외 일정을 가져오는 요청
	            $.ajax({
	                url: 'calendar/events?projectNum=' + projectNum, // 업무 외 일정을 가져올 API 경로
	                method: 'GET',
	                dataType: 'json',
	                success: function(externalEvents) {
						console.log('externalEvents 체크용 : ', externalEvents);
						
	                    // 외부 이벤트를 캘린더에 추가
	                    externalEvents.forEach(event => {
	                        let eventColor;

	                        // 색상 설정: 이벤트 유형에 따라 구분
	                        if (event.eventType === '1') { // 반복 일정
	                            eventColor = 'blue'; // 반복 일정의 색상

	                            const startDate = new Date(event.startDate); // 시작 날짜
	                            const endDate = new Date(event.endDate); // 종료 날짜

	                            // 시작 날짜부터 종료일 전날까지 반복
	                            while (startDate < endDate) {
	                                const eventStart = new Date(startDate);
	                                const eventEnd = new Date(startDate);

	                                eventStart.setHours(event.startDate.split('T')[1].split(':')[0], event.startDate.split('T')[1].split(':')[1], 0);
	                                eventEnd.setHours(event.endDate.split('T')[1].split(':')[0], event.endDate.split('T')[1].split(':')[1], 0);

	                                // 이벤트 추가
	                                calendar.addEvent({
	                                    title: event.title,
	                                    start: eventStart.toISOString(),
	                                    end: eventEnd.toISOString(),
	                                    color: eventColor,
	                                    extendedProps: {
	                                        eventId: event.eventId, // eventId 추가
											eventType: event.eventType // eventType 추가
	                                    }
	                                });

	                                // 다음 날로 이동
	                                startDate.setDate(startDate.getDate() + 1);
	                            }
	                        } else if (event.eventType === '2') { // 단기 일정
	                            eventColor = 'green'; // 단기 일정의 색상
	                            calendar.addEvent({
	                                title: event.title,
	                                start: event.startDate,
	                                end: event.endDate,
	                                color: eventColor,
	                                extendedProps: {
	                                    eventId: event.eventId, // eventId 추가
										eventType: event.eventType // eventType 추가
	                                }
	                            });
	                        }
	                    });

	                    // 캘린더 렌더링
	                    calendar.render();
	                },
	                error: function(xhr) {
	                    console.error('업무 외 일정 로드 실패:', xhr.responseText); // 에러 로그
	                    alert('업무 외 일정을 로드하는 데 실패했습니다.'); // 사용자 알림
	                }
	            });
	        },
	        error: function(xhr) {
	            console.error('업무 로드 실패:', xhr.responseText); // 에러 로그
	            alert('업무 데이터를 로드하는 데 실패했습니다.'); // 사용자 알림
	        }
	    });
	}

	// 상태(Status)에 따라 색상을 반환하는 함수
	function getColorByStatus(status) {
	    switch (status) {
	        case 'COMPLETED':
	            return 'green'; // 완료
	        case 'INPROGRESS':
	            return 'blue'; // 진행 중
	        case 'HOLD':
	            return 'yellow'; // 보류
	        case 'REQUEST':
	            return 'orange'; // 요청
	        case 'FEEDBACK':
	            return 'purple'; // 피드백
	        default:
	            return 'gray'; // 기본 색상
	    }
	}

	// 선택 모달 이벤트 설정
    calendarEl.addEventListener('dateClick', function(info) {
        document.getElementById('select-modal').classList.remove('hidden');
    });

    // 업무 등록 버튼 클릭 시
    document.getElementById('add-task').addEventListener('click', function() {
        document.getElementById('select-modal').classList.add('hidden');
        document.getElementById('task-modal').classList.remove('hidden');
		
		// 선택한 날짜를 업무 등록 모달에 설정할 수 있음
	    document.getElementById('task-start-date').value = selectedDate;
	    document.getElementById('task-end-date').value = selectedDate;
    });

    // 캘린더 업무 외 일정 추가 버튼 클릭 시
    document.getElementById('add-event').addEventListener('click', function() {
        document.getElementById('select-modal').classList.add('hidden');
        document.getElementById('event-modal').classList.remove('hidden');
		
		// 선택한 날짜를 일정 추가 모달에 설정할 수 있음
	    document.getElementById('event-start-date').value = selectedDate;
	    document.getElementById('event-end-date').value = selectedDate;
    });

    // 모달 닫기
    document.getElementById('close-select-modal').addEventListener('click', function() {
        document.getElementById('select-modal').classList.add('hidden');
    });
	
	// 모달 닫기 함수
	function closeModal(modalId) {
	    document.getElementById(modalId).classList.add('hidden');
	}

	// 'event-modal' 닫기 버튼
	document.getElementById('close-event-modal').addEventListener('click', function() {
	    closeModal('event-modal');
	});

	// 일정 추가 폼 제출 처리
	document.getElementById('event-form').addEventListener('submit', function(event) {
	    event.preventDefault();
	    
	    const title = document.getElementById('event-title').value;
	    const type = document.getElementById('event-type').value; // 일정 유형
	    const startDate = document.getElementById('event-start-date').value;
	    const endDate = document.getElementById('event-end-date').value;
	    const startTime = document.getElementById('event-start-time').value;
	    const endTime = document.getElementById('event-end-time').value;

	    // 중복 일정 체크를 위한 이벤트 가져오기
	    const events = calendar.getEvents();
	    const isDuplicate = (start, end) => {
	        return events.some(event =>
	            (event.title === title && 
	            event.startStr === start && 
	            event.endStr === end)
	        );
	    };

	    // 일정 등록 처리
	    if (title && startDate && endDate) {
	        let eventAdded = false; // 이벤트 추가 여부 플래그

	        if (type === '1') {
	            // 일일 일정 (반복 일정)
	            let currentDate = new Date(startDate);
	            const endDateObj = new Date(endDate);

	            while (currentDate <= endDateObj) {
	                const eventStart = `${currentDate.toISOString().split('T')[0]}T${startTime}:00`;
	                const eventEnd = `${currentDate.toISOString().split('T')[0]}T${endTime}:00`;

	                if (!isDuplicate(eventStart, eventEnd)) {
	                    calendar.addEvent({
	                        title: title,
	                        start: eventStart,
	                        end: eventEnd,
							color: 'blue' // 반복 일정 색상
	                    });
	                    eventAdded = true; // 이벤트 추가됨
	                }
	                currentDate.setDate(currentDate.getDate() + 1);
	            }
	        } else if (type === '2') {
	            // 단일 기간 일정
	            const startDateTime = `${startDate}T${startTime}:00`;
	            const endDateTime = `${endDate}T${endTime}:00`;

	            if (!isDuplicate(startDateTime, endDateTime)) {
	                calendar.addEvent({
	                    title: title,
	                    start: startDateTime,
	                    end: endDateTime,
						color: 'green' // 단일 기간 일정 색상
	                });
	                eventAdded = true; // 이벤트 추가됨
	            }
	        }

	        // AJAX 요청 처리 (서버에 일정 저장)
	        const eventData = {
	            title: title,
	            startDate: `${startDate}T${startTime}:00`,
	            endDate: `${endDate}T${endTime}:00`,
	            eventType: type,
	            projectNum: projectNum // 현재 프로젝트 번호 전달
	        };

	        $.ajax({
	            url: 'calendar/event',
	            type: 'POST',
	            contentType: 'application/json',
	            data: JSON.stringify(eventData),
	            success: function(savedEvent) {
					console.log("savedEvent 체크용 : ", savedEvent)
					
	                // 서버에서 반환된 이벤트 추가
	                if (!eventAdded) {
	                    calendar.addEvent({
	                        title: savedEvent.title,
	                        start: savedEvent.startDate,
	                        end: savedEvent.endDate,
							extendedProps: {
			                                    eventId: savedEvent.eventId, // eventId 추가
												eventType: savedEvent.eventType // eventType 추가
			                                }
	                    });
	                }
	                // 모달 닫기 및 폼 리셋
	                closeModal('event-modal');
	                document.getElementById('event-form').reset();
					
					loadCalendar()
	            },
	            error: function(xhr) {
	                console.error('일정 등록 실패:', xhr.responseText);
	                alert('일정을 등록하는 데 실패했습니다.');
	            }
	        });
	    }
	});

	// 일정 상세 모달 열기
	function openEventDetailModal(event) {

	    const currentEventId = event.extendedProps.eventId; // FullCalendar 이벤트의 eventId 가져오기
	    console.log("eventId 확인용 : ", currentEventId);

	    const modal = document.createElement('div');
	    modal.classList.add('modal');

	    const modalContent = document.createElement('div');
	    modalContent.classList.add('modal-content');

	    const title = document.createElement('h3');
	    title.textContent = event.title;

	    const details = document.createElement('p');
	    details.textContent = formatEventDetails(event);

	    const closeButton = document.createElement('button');
	    closeButton.textContent = 'Close';
	    closeButton.classList.add('btn-close');
	    closeButton.addEventListener('click', () => {
	        document.body.removeChild(modal);
	    });

	    const deleteButton = document.createElement('button');
	    deleteButton.textContent = 'Delete';
	    deleteButton.classList.add('btn-delete');
	    deleteButton.addEventListener('click', () => {
			// 반복 일정인지 확인
		    if (isRecurringEvent(event)) {
		        const confirmDelete = confirm('이 반복 일정을 모두 삭제하시겠습니까?');
		        if (!confirmDelete) return; // 사용자가 삭제를 원하지 않을 경우 종료
		    }
			
	        // 서버에 삭제 요청
		    $.ajax({
		        url: 'calendar/deleteEvent?eventId=' + currentEventId, // URL에 eventId 포함
		        type: 'POST', // POST 방식
		        success: function() {
		            // 삭제 성공 시 캘린더에서 반복 일정의 모든 인스턴스 제거
		            removeAllRecurringEvents(event); 
		            document.body.removeChild(modal); // 모달 닫기
		            alert('일정이 삭제되었습니다.'); // 사용자에게 알림
		        },
		        error: function(xhr) {
		            console.error('일정 삭제 실패:', xhr.responseText);
		            const errorMessage = xhr.status === 404 ? '일정을 찾을 수 없습니다.' : '일정을 삭제하는 데 실패했습니다.';
		            alert(errorMessage); // 에러 메시지
		        }
		    });
	    });

	    const buttonsContainer = document.createElement('div');
	    buttonsContainer.classList.add('modal-buttons');
	    buttonsContainer.appendChild(closeButton);
	    buttonsContainer.appendChild(deleteButton);

	    modalContent.appendChild(title);
	    modalContent.appendChild(details);
	    modalContent.appendChild(buttonsContainer);
	    modal.appendChild(modalContent);

	    document.body.appendChild(modal);
	}
	
	// 반복 일정 확인 함수
	function isRecurringEvent(event) {
	    return event.extendedProps.eventType === '1'; // 1은 반복 일정을 의미
	}
	
	// 모든 반복 일정 제거 함수 구현
	function removeAllRecurringEvents(event) {
	    const eventId = event.extendedProps.eventId;

	    // 반복 일정의 모든 인스턴스를 삭제
	    calendar.getEvents().forEach(calEvent => {
	        if (calEvent.extendedProps.eventId === eventId) {
	            calEvent.remove(); // 이벤트 삭제
	        }
	    });
	}
	
	
	/*
    // 이벤트 폼 제출 핸들러
    function handleEventFormSubmit(event) {
        event.preventDefault();
		
        const title = document.getElementById('event-title').value;
        const type = document.getElementById('event-type').value;
        const startDate = document.getElementById('event-start-date').value;
        const endDate = document.getElementById('event-end-date').value;
        const startTime = document.getElementById('event-start-time').value;
        const endTime = document.getElementById('event-end-time').value;

		console.log(`Event submitted - Title: ${title}, Type: ${type}, Start Date: ${startDate}, End Date: ${endDate}, Start Time: ${startTime}, End Time: ${endTime}`);
		
        if (title && startDate && endDate) {
            let startDateTime, endDateTime;

            // 중복 일정 체크
            const events = calendar.getEvents();
            const isDuplicate = (start, end) => {
                return events.some(event =>
                    (event.title === title && 
                    event.startStr === start && 
                    event.endStr === end)
                );
            };

            if (type === '1') {
                // 일일 일정 (반복 일정)
                const startOfDayTime = `${startDate}T${startTime}:00`;
                const endOfDayTime = `${endDate}T${endTime}:00`;

                let currentDate = new Date(startDate);
                const endDateObj = new Date(endDate);

                while (currentDate <= endDateObj) {
                    const eventStart = `${currentDate.toISOString().split('T')[0]}T${startTime}:00`;
                    const eventEnd = `${currentDate.toISOString().split('T')[0]}T${endTime}:00`;

                    if (!isDuplicate(eventStart, eventEnd)) {
                        calendar.addEvent({
                            title: title,
                            start: eventStart,
                            end: eventEnd
                        });
                    }
                    currentDate.setDate(currentDate.getDate() + 1);
                }
            } else if (type === '2') {
                // 시간 기반 일정
                startDateTime = `${startDate}T${startTime}:00`;
                endDateTime = `${endDate}T${endTime}:00`;

                if (!isDuplicate(startDateTime, endDateTime)) {
                    calendar.addEvent({
                        title: title,
                        start: startDateTime,
                        end: endDateTime
                    });
                }
            }

            // 모달 닫기 및 폼 리셋
            document.getElementById('event-modal').classList.add('hidden');
            document.getElementById('event-form').reset();
        }
    }

    // 일정 상세 모달 열기
    function openEventDetailModal(event) {
        const modal = document.createElement('div');
        modal.classList.add('modal');

        const modalContent = document.createElement('div');
        modalContent.classList.add('modal-content');

        const title = document.createElement('h3');
        title.textContent = event.title;

        const details = document.createElement('p');
        details.textContent = formatEventDetails(event);

        const closeButton = document.createElement('button');
        closeButton.textContent = 'Close';
        closeButton.classList.add('btn-close');
        closeButton.addEventListener('click', () => {
            document.body.removeChild(modal);
        });

        const deleteButton = document.createElement('button');
        deleteButton.textContent = 'Delete';
        deleteButton.classList.add('btn-delete');
        deleteButton.addEventListener('click', () => {
            event.remove();
            document.body.removeChild(modal);
        });

        const buttonsContainer = document.createElement('div');
        buttonsContainer.classList.add('modal-buttons');
        buttonsContainer.appendChild(closeButton);
        buttonsContainer.appendChild(deleteButton);

        modalContent.appendChild(title);
        modalContent.appendChild(details);
        modalContent.appendChild(buttonsContainer);
        modal.appendChild(modalContent);

        document.body.appendChild(modal);
    }
	*/

    // 일정 상세 내용 포맷팅
    function formatEventDetails(event) {
        const startDate = new Date(event.start);
        const endDate = event.end ? new Date(event.end) : null;
        const startDateStr = formatDate(startDate);
        const endDateStr = endDate ? formatDate(endDate) : 'No end time';
        return `${startDateStr} ~ ${endDateStr}`;
    }

    // 날짜 포맷팅
    function formatDate(date) {
        const options = {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            hour12: true
        };
        return new Intl.DateTimeFormat('en-US', options).format(date);
    }
	
	
	// 간트차트 데이터 로드
	function importGanttChartData() {
	    // 페이지 로드 시 자동으로 데이터 로드 및 차트 생성
	    $.ajax({
	        url: 'getGanttChartData?projectNum=' + projectNum, // URL 쿼리 파라미터로 전송
	        type: 'GET',
	        dataType: 'json',
	        success: function(response) {
	            // 응답 데이터 검증
	            if (response.data && Array.isArray(response.data)) {
	                const data = response.data;

	                console.log("data 체크용:", data);
	                
	                // 간트차트 생성 함수 호출
	                createGanttChart(data);
	            } else {
	                alert('간트 차트 데이터를 로드하는 데 필요한 정보가 부족합니다.');
	                console.error('잘못된 데이터:', response);
	            }
	        },
	        error: function(xhr, status, error) {
	            console.error('AJAX Error:', status, error);
	            alert('서버 요청 중 오류가 발생했습니다.');
	        }
	    });
	}

	// 간트차트 생성(로드)
	function createGanttChart(ganttChartData) {
	    console.log('createGanttChart 호출:');
	    console.log('간트차트 데이터:', ganttChartData); // 데이터 확인 로그

	    if (ganttChartLoaded) {
	        // 기존 차트가 로드되어 있으면 제거
	        ganttChart.dispose();
	    }

	    anychart.onDocumentReady(function () {
	        // 프로젝트 Gantt 차트 생성
	        ganttChart = anychart.ganttProject();

	        // 데이터 트리 생성
	        let treeData = anychart.data.tree(ganttChartData, "as-tree");

	        console.log('TreeData 체크용:', treeData);

	        // 각 기능의 actualStart와 actualEnd 업데이트
	        ganttChartData.forEach(functionData => {
	            const children = functionData.children;

	            // 업무들 중에서 가장 빠른 actualStart를 찾습니다.
	            const functionActualStart = children
	                .map(task => {
	                    // actualStart가 없으면 baselineStart 사용
	                    return task.actualStart ? new Date(task.actualStart) : (task.baselineStart ? new Date(task.baselineStart) : null);
	                })
	                .filter(task => task !== null)
	                .reduce((min, curr) => (min === null || curr < min ? curr : min), null);

	            // 업무들 중에서 가장 늦은 actualEnd를 찾습니다.
	            const functionActualEnd = children
	                .map(task => {
	                    // actualEnd가 없으면 baselineEnd 사용
	                    return task.actualEnd ? new Date(task.actualEnd) : (task.baselineEnd ? new Date(task.baselineEnd) : null);
	                })
	                .filter(task => task !== null)
	                .reduce((max, curr) => (max === null || curr > max ? curr : max), null);

	            // 해당 기능의 actualStart와 actualEnd를 업데이트합니다.
	            functionData.actualStart = functionActualStart ? functionActualStart.toISOString() : null;
	            functionData.actualEnd = functionActualEnd ? functionActualEnd.toISOString() : null;
	        });

	        // 차트에 데이터 설정
	        ganttChart.data(treeData);

	        console.log("ganttChart 확인: ", ganttChart);

	        // 분할기 위치 설정
	        ganttChart.splitterPosition(550); // 분할기 위치를 550px로 설정하여 고정

	        // 차트 데이터 그리드 링크를 가져와서 열 설정
	        let dataGrid = ganttChart.dataGrid();

	        // 첫 번째 열 설정
	        dataGrid
	            .column(0)
	            .title('ID')
	            .width(30)
	            .labels({ hAlign: 'center' });

	        // 두 번째 열 설정
	        dataGrid
	            .column(1)
	            .width(200)
	            .labelsOverrider(labelTextSettingsFormatter);

	        // 세 번째 열 설정
	        dataGrid
	            .column(2)
	            .title('Planned Start')
	            .width(150)
	            .labelsOverrider(labelTextSettingsFormatter)
	            .labels()
	            .format(thirdColumnTextFormatter);

	        // 네 번째 열 설정
	        dataGrid
	            .column(3)
	            .title('Planned End')
	            .width(150)
	            .labelsOverrider(labelTextSettingsFormatter)
	            .labels()
	            .format(fourthColumnTextFormatter);

	        // 타임라인 객체를 가져옵니다
	        timeline = ganttChart.getTimeline();
	        timeline.baselines().above(true); // 기준선이 행 위에 위치하도록 설정합니다
	        timeline.milestones().preview().enabled(true); // 이정표 미리보기를 활성화합니다
	        timeline.baselineMilestones().preview().enabled(true); // 기준선 이정표 미리보기를 활성화합니다

	        // 각 업무의 baselineStart 및 actualStart 중 가장 빠른 시작일 및 가장 늦은 종료일을 계산
	        let earliestStart = new Date(Math.min(
	            ...ganttChartData.flatMap(item => [
	                new Date(item.baselineStart).getTime(),
	                new Date(item.actualStart).getTime(),
	                ...item.children.map(child => [
	                    new Date(child.baselineStart).getTime(),
	                    new Date(child.actualStart).getTime()
	                ])
	            ]).flat() // 평탄화
	        ));

	        let latestEnd = new Date(Math.max(
	            ...ganttChartData.flatMap(item => [
	                new Date(item.baselineEnd).getTime(),
	                new Date(item.actualEnd).getTime(),
	                ...item.children.map(child => [
	                    new Date(child.baselineEnd).getTime(),
	                    new Date(child.actualEnd).getTime()
	                ])
	            ]).flat() // 평탄화
	        ));

	        console.log('earliestStart 체크용:', earliestStart);
	        console.log('latestEnd 체크용:', latestEnd);

	        // 유효한 날짜인지 확인
	        if (isNaN(earliestStart.getTime())) {
	            console.warn('유효하지 않은 가장 빠른 시작일:', earliestStart);
	        }
	        if (isNaN(latestEnd.getTime())) {
	            console.warn('유효하지 않은 가장 늦은 종료일:', latestEnd);
	        }

	        // 여유 기간을 설정합니다
	        const bufferDays = 30;

	        // 여유 기간을 추가합니다
	        earliestStart.setUTCDate(earliestStart.getUTCDate() - bufferDays);
	        latestEnd.setUTCDate(latestEnd.getUTCDate() + bufferDays);

	        console.log("조정된 시작일:", earliestStart.toISOString());
	        console.log("조정된 종료일:", latestEnd.toISOString());

	        // Gantt 차트를 HTML 컨테이너에 렌더링합니다
	        ganttChart.container('gantt-chart'); // 컨테이너 ID를 수정하세요
	        ganttChart.draw();

	        // 차트의 날짜 범위를 조정합니다
	        ganttChart.zoomTo(earliestStart.getTime(), latestEnd.getTime());
	        timeline.scale().minimum(earliestStart.getTime());
	        timeline.scale().maximum(latestEnd.getTime());

	        console.log("Zoom Start:", earliestStart.getTime());
	        console.log("Zoom End:", latestEnd.getTime());

	        ganttChart.fitAll();
	        ganttChartLoaded = true;
	    });
		
		// loadEntityNames(진행도)
		function loadNamesForProgress(projectNum, entityType) {
		    console.log(`Loading names for progress select box, projectNum: ${projectNum}, entityType: ${entityType}`); // 디버깅용 로그
		    
		    $.ajax({
		        url: 'loadEntityNames',
		        type: 'GET',
		        data: { projectNum: projectNum, entityType: entityType },
		        success: function(response) {
		            console.log('Response for progress select box:', response); // 디버깅용 로그
		            
		            const $selectProgress = $('#name-select');
		            $selectProgress.empty().append('<option value="">선택</option>'); 
		            
		            response.forEach(function(item) {
		                if (entityType === 'task' && item.taskId && item.taskTitle) {
		                    $selectProgress.append(`<option value="${item.taskId}">${item.taskTitle}</option>`);
		                } else if (entityType === 'function' && item.functionTitleId && item.titleName) {
		                    $selectProgress.append(`<option value="${item.functionTitleId}">${item.titleName}</option>`);
		                }
		            });
		        },
		        error: function(xhr, status, error) {
		            console.error('Error loading progress names:', error); // 디버깅용 로그
		            alert('진행도 이름 목록을 불러오는 데 실패했습니다.');
		        }
		    });
		}
		
		// loadEntityNames(실제 진행 일정)
		function loadNamesForDate(projectNum, entityType) {
		    console.log(`Loading names for date select box, projectNum: ${projectNum}, entityType: ${entityType}`); // 디버깅용 로그
		    
		    $.ajax({
		        url: 'loadEntityNames',
		        type: 'GET',
		        data: { projectNum: projectNum, entityType: entityType },
		        success: function(response) {
		            console.log('Response for date select box:', response); // 디버깅용 로그
		            
		            const $selectDate = $('#name-selectDate');
		            $selectDate.empty().append('<option value="">선택</option>'); 
		            
		            response.forEach(function(item) {
		                if (entityType === 'task' && item.taskId && item.taskTitle) {
		                    $selectDate.append(`<option value="${item.taskId}">${item.taskTitle}</option>`);
		                } else if (entityType === 'function' && item.functionTitleId && item.titleName) {
		                    $selectDate.append(`<option value="${item.functionTitleId}">${item.titleName}</option>`);
		                }
		            });
		        },
		        error: function(xhr, status, error) {
		            console.error('Error loading date names:', error); // 디버깅용 로그
		            alert('일정 이름 목록을 불러오는 데 실패했습니다.');
		        }
		    });
		}

		// 페이지 로드 시 기본값으로 'task'를 선택하여 이름을 로드
		$(document).ready(function() {
		    let projectNumber = new URLSearchParams(window.location.search).get('projectNum');
		    if (projectNumber) {
		        $('#entityType-select').val('task');  // 기본값으로 'task' 선택
				$('#entityType-selectDate').val('task'); // 기본값으로 'task' 선택
				loadNamesForProgress(projectNumber, 'task');  // 기본값으로 'task' 로드
				loadNamesForDate(projectNum, 'task'); // 기본값으로 'task' 로드
			}
			
			// 기존 이벤트 리스너를 제거하고 새로운 리스너를 추가
			$('#update-progress-button').off('click').on('click', updateProgress);
			$('#update-schedule-button').off('click').on('click', updateActualDate);
			
			// Entity Type select box 변경 시 처리 (진행도)
			$('#entityType-select').change(function() {
			    const entityType = $(this).val();
			    const projectNum = getQueryParam('projectNum'); // URL 쿼리 스트링에서 projectNum을 가져옴

			    console.log(`Entity Type Selected for progress: ${entityType}`); // 디버깅용 로그
			    console.log(`Project Num: ${projectNum}`); // 디버깅용 로그
			    
			    if (entityType && projectNum) {
			        loadNamesForProgress(projectNum, entityType);
			        $('#name-select').prop('disabled', false);
			    } else {
			        $('#name-select').prop('disabled', true);
			        $('#name-select').empty().append('<option value="">선택</option>');
			    }
			});

			// Entity Type select box 변경 시 처리 (실제 일정 업데이트)
			$('#entityType-selectDate').change(function() {
			    const entityType = $(this).val();
			    const projectNum = getQueryParam('projectNum'); // URL 쿼리 스트링에서 projectNum을 가져옴

			    console.log(`Entity Type Selected for date: ${entityType}`); // 디버깅용 로그
			    console.log(`Project Num: ${projectNum}`); // 디버깅용 로그
			    
			    if (entityType && projectNum) {
			        loadNamesForDate(projectNum, entityType);
			        $('#name-selectDate').prop('disabled', false);
			    } else {
			        $('#name-selectDate').prop('disabled', true);
			        $('#name-selectDate').empty().append('<option value="">선택</option>');
			    }
			});

		});

		// 진행도 업데이트 함수
		function updateProgress() {
		    let ganttId = $('#name-select').val();
		    let entityType = $('#entityType-select').val();
		    let progressValue = $('#progress-value').val() + "%"; // 스크롤바 값을 가져와서 "%" 추가

		    // 입력 값 유효성 검사
		    if (!ganttId || !entityType || !progressValue) {
		        alert('모든 필드를 입력해 주세요');
		        return;
		    }

		    // AJAX 요청을 통해 서버에 진행도 업데이트 요청
		    $.ajax({
		        url: 'updateProgress',
		        type: 'POST',
		        data: {
		            entityType: entityType,
		            id: ganttId,  // 실제 DB ID
		            progressValue: progressValue
		        },
		        success: function(response) {
		            if (response === 'success') {
		                alert('진행도가 업데이트되었습니다.');

		                // 간트차트를 새로고침하여 반영된 진행도 확인
		                refreshGanttChart();
		                
		                // 입력 박스와 select 박스 값을 초기화
		                $('#progress-value').val(0); // 스크롤바 초기화
		                // $('#progress-value-display').text('0%'); // 진행도 표시 초기화
						
						updateProgressDisplay();
						
		            } else {
		                alert('진행도 업데이트에 실패했습니다.');
		            }
		        },
		        error: function(xhr, status, error) {
		            console.error('AJAX Error:', status, error);
		            alert('서버 요청 중 오류가 발생했습니다.');
		        }
		    });
		}
		
		// 실제 일정 업데이트 버튼 클릭 이벤트 핸들러
		function updateActualDate() {
		    let ganttId = $('#name-selectDate').val();
		    let entityType = $('#entityType-selectDate').val();
		    let actualStart = $('#actual-start-date').val();
		    let actualEnd = $('#actual-end-date').val();

		    // 입력 값 유효성 검사
		    if (!ganttId || !entityType || !actualStart || !actualEnd) {
		        alert('모든 필드를 입력해 주세요');
		        return;
		    }

		    // 로컬 시간을 UTC로 변환
		    const startDate = new Date(actualStart);
		    const endDate = new Date(actualEnd);

		    // UTC ISO 문자열 생성
		    const formattedStart = new Date(startDate.getTime() - (startDate.getTimezoneOffset() * 60000)).toISOString().split('.')[0] + 'Z';
		    const formattedEnd = new Date(endDate.getTime() - (endDate.getTimezoneOffset() * 60000)).toISOString().split('.')[0] + 'Z';

		    // AJAX 요청을 통해 서버에 실제 일정 업데이트 요청
		    $.ajax({
		        url: 'updateSchedule',
		        type: 'POST',
		        data: {
		            entityType: entityType,
		            id: ganttId,
		            actualStart: formattedStart,
		            actualEnd: formattedEnd
		        },
		        success: function(response) {
		            console.log('업데이트 응답:', response);
		            if (response === 'success') {
		                alert('실제 일정이 업데이트되었습니다.');
		                refreshGanttChart(ganttId, entityType, formattedStart, formattedEnd);
		                $('#actual-start-date').val(''); // 초기화
		                $('#actual-end-date').val('');   // 초기화
		            } else {
		                alert('실제 일정 업데이트에 실패했습니다.');
		            }
		        },
		        error: function(xhr, status, error) {
		            console.error('AJAX Error:', status, error);
		            console.log('Sent Data:', { entityType, ganttId, actualStart: formattedStart, actualEnd: formattedEnd });
		            
		            if (xhr.status === 400) {
		                alert('유효하지 않은 Entity 유형입니다.');
		            } else if (xhr.status === 404) {
		                alert('작업을 찾을 수 없습니다. ID: ' + ganttId);
		            } else {
		                alert('서버 요청 중 오류가 발생했습니다.');
		            }
		        }
		    });
		}

		// 간트차트 새로고침 함수
		function refreshGanttChart(ganttId, entityType, actualStart, actualEnd) {
		    const projectNum = new URLSearchParams(window.location.search).get('projectNum');
		    if (!projectNum) {
		        alert('프로젝트 번호가 없습니다.');
		        return;
		    }

		    $.ajax({
		        url: 'getGanttChartData?projectNum=' + projectNum, // URL 쿼리 파라미터로 전송
		        type: 'GET',
		        dataType: 'json',
		        success: function(response) {
		            // 응답 데이터 검증
		            if (response && response.data && Array.isArray(response.data) && response.data.length > 0) {

		                // 데이터 추출
		                const data = response.data;

		                console.log('data 응답 check:', data);
		                
		                // 업데이트된 값 적용
		                data.forEach(function(functionItem) {
		                    // Function Title의 자식 업무 업데이트
		                    if (functionItem.children) {
		                        functionItem.children.forEach(function(child) {
		                            if (child.dbId === ganttId) {
		                                // baseline 값은 초기 데이터에서 가져오기
		                                const originalBaselineStart = child.baselineStart; // 원본 baselineStart 저장
		                                const originalBaselineEnd = child.baselineEnd;     // 원본 baselineEnd 저장

		                                // actualStart와 actualEnd만 업데이트 (시간 포함)
		                                child.actualStart = actualStart + 'T' + child.actualStart.split('T')[1]; // 시간 부분 유지
		                                child.actualEnd = actualEnd + 'T' + child.actualEnd.split('T')[1];       // 시간 부분 유지

		                                // baseline 값을 원래대로 유지
		                                child.baselineStart = originalBaselineStart;
		                                child.baselineEnd = originalBaselineEnd;
		                            }
		                        });
		                    }
		                });

		                // 기능 자체의 actualStart와 actualEnd 업데이트
		                if (entityType === 'function') {
		                    data.forEach(function(functionItem) {
		                        if (functionItem.dbId === ganttId) {
		                            functionItem.actualStart = actualStart + 'T' + functionItem.actualStart.split('T')[1]; // 시간 부분 유지
		                            functionItem.actualEnd = actualEnd + 'T' + functionItem.actualEnd.split('T')[1];       // 시간 부분 유지
		                        }
		                    });
		                }

		                console.log("refresh data 체크용: ", data);

		                // 간트차트 생성 함수 호출
		                createGanttChart(data);
		            } else {
		                alert('간트 차트 데이터를 로드하는 데 필요한 정보가 부족합니다.');
		                console.error('잘못된 데이터:', response);
		            }
		        },
		        error: function(xhr, status, error) {
		            console.error('AJAX 오류:', status, error);
		            alert('서버 요청 중 오류가 발생했습니다.');
		        }
		    });
		}
	
	    // 모든 부모 항목에 굵고 기울임꼴 텍스트 설정 추가
	    function labelTextSettingsFormatter(label, dataItem) {
	        if (dataItem.numChildren()) {
	        	label.fontWeight('bold').fontStyle('italic');
	    	}
		}
	
		// 세 번째 열의 날짜를 예쁘게 포맷팅
		function thirdColumnTextFormatter(data) {
		    let field = data.baselineStart;
	
		    // 기준선 텍스트 포맷팅
		    if (field) {
		        let baselineStart = new Date(field);
		        return (
	                formatDate(baselineStart.getUTCMonth() + 1) +
	                '/' +
	                formatDate(baselineStart.getUTCDate()) +
	                '/' +
	                baselineStart.getUTCFullYear() +
	                ' ' +
	                formatDate(baselineStart.getUTCHours()) +
	                ':' +
	                formatDate(baselineStart.getUTCMinutes())
	            );
	        }
	
	        // 이정표 텍스트 포맷팅
	        let actualStart = data.item.get('actualStart');
	        let actualEnd = data.item.get('actualEnd');
	        if (actualStart === actualEnd || (actualStart && !actualEnd)) {
	            let start = new Date(actualStart);
	            return (
	                formatDate(start.getUTCMonth() + 1) +
	                '/' +
	                formatDate(start.getUTCDate()) +
	                '/' +
	                start.getUTCFullYear() +
	                ' ' +
	                formatDate(start.getUTCHours()) +
	                ':' +
	                formatDate(start.getUTCMinutes())
	            );
	        }
	        return '';
	    }
	
	    // 네 번째 열의 날짜를 예쁘게 포맷팅
	    function fourthColumnTextFormatter(item) {
	        let field = item.baselineEnd;
	        if (field) {
	            let baselineEnd = new Date(field);
	            return (
	                formatDate(baselineEnd.getUTCMonth() + 1) +
	                '/' +
	                formatDate(baselineEnd.getUTCDate()) +
	                '/' +
	                baselineEnd.getUTCFullYear() +
	                ' ' +
	                formatDate(baselineEnd.getUTCHours()) +
	                ':' +
	                formatDate(baselineEnd.getUTCMinutes())
	            );
	        }
	        return '';
	    }
	
	    // 날짜 단위를 예쁘게 포맷팅
	    function formatDate(dateUnit) {
	        if (dateUnit < 10) dateUnit = '0' + dateUnit;
	        return dateUnit + '';
	    }
	}

});

	/*
	// 간트차트 생성(로드)
	function createGanttChart() {
	  if (ganttChartLoaded) return; // 이미 간트 차트가 로드된 경우

	  anychart.onDocumentReady(function () {
	    // 직접 포함된 데이터
		ganttChartData = [
		  {
		    "id": 1,
		    "name": "Pre-planning",
		    "actualStart": "2024-09-01T08:00:00Z",
		    "actualEnd": "2024-09-07T18:00:00Z",
		    "progressValue": "0%",
		    "baselineStart": "2024-08-30T09:00:00Z",
		    "baselineEnd": "2024-09-05T17:00:00Z",
		    "rowHeight": 35,
		    "children": [
		      {
		        "id": 2,
		        "name": "Investigate the task",
		        "actualStart": "2024-09-01T07:00:00Z",
		        "actualEnd": "2024-09-05T19:00:00Z",
		        "progressValue": "0%",
		        "baselineStart": "2024-09-02T08:00:00Z",
		        "baselineEnd": "2024-09-04T20:00:00Z",
		        "rowHeight": 35,
				"connector": [
					{
						"connectTo": 3
					}
				]
		      },
		      {
		        "id": 3,
		        "name": "Distribute roles and resources",
		        "actualStart": "2024-09-06T07:00:00Z",
		        "actualEnd": "2024-09-10T16:00:00Z",
		        "progressValue": "0%",
		        "baselineStart": "2024-09-07T10:00:00Z",
		        "baselineEnd": "2024-09-11T21:00:00Z",
		        "rowHeight": 35,
				"connector": [
					{
						"connectTo": 4
					}
				]
		      },
		      {
		        "id": 4,
		        "name": "Gather technical documentation",
		        "actualStart": "2024-09-11T09:00:00Z",
		        "actualEnd": "2024-09-15T18:00:00Z",
		        "progressValue": "0%",
		        "baselineStart": "2024-09-12T11:00:00Z",
		        "baselineEnd": "2024-09-17T22:00:00Z",
		        "rowHeight": 35
		      }
		    ]
		  }
		];

	    // 프로젝트 Gantt 차트 생성
	    ganttChart = anychart.ganttProject();

		// 데이터 트리 생성
		let treeData = anychart.data.tree(ganttChartData, "as-tree");

	    // 차트에 데이터 설정
	    ganttChart.data(treeData);

		console.log("ganttChart 확인: ", ganttChart);

		// 분할기 위치 설정
		ganttChart.splitterPosition(550); // 분할기 위치를 550px로 설정하여 고정

	    // 차트 데이터 그리드 링크를 가져와서 열 설정
	    let dataGrid = ganttChart.dataGrid();

	    // 첫 번째 열 설정
		dataGrid
			.column(0)
			.title('ID')
			.width(30)
			.labels({ hAlign: 'center' });

	    // 두 번째 열 설정
	    dataGrid
	      .column(1)
	      .width(200)
	      .labelsOverrider(labelTextSettingsFormatter);

	    // 세 번째 열 설정
	    dataGrid
	      .column(2)
	      .title('Planned Start')
	      .width(150)
	      .labelsOverrider(labelTextSettingsFormatter)
	      .labels()
	      .format(thirdColumnTextFormatter);

	    // 네 번째 열 설정
	    dataGrid
	      .column(3)
	      .title('Planned End')
	      .width(150)
	      .labelsOverrider(labelTextSettingsFormatter)
	      .labels()
	      .format(fourthColumnTextFormatter);

	    timeline = ganttChart.getTimeline();

	    // 기준선이 행 위에 위치하도록 설정
	    timeline.baselines().above(true);

	    // 이정표 미리보기 활성화
	    timeline.milestones().preview().enabled(true);
	    timeline.baselineMilestones().preview().enabled(true);

		let chartLastElement = ganttChartData[ganttChartData.length - 1];

		console.log("시작일 형식: ", ganttChartData[0].actualStart);
		console.log("종료일 형식: ", chartLastElement.children[chartLastElement.children.length - 1].actualEnd);

		startDateStr = ganttChartData[0].actualStart;
		endDateStr = chartLastElement.children[chartLastElement.children.length - 1].actualEnd;

		// 날짜 문자열을 Date 객체로 변환 (가정: 날짜 문자열은 "YYYY-MM-DD" 형식)
		startDate = new Date(startDateStr);
		endDate = new Date(endDateStr);

		// 여유 기간을 추가할 일수 (1주일 = 7일)
		const bufferDays = 7;

		// 시작일과 종료일에 여유 기간 추가
		let adjustedStartDate = new Date(startDate);
		adjustedStartDate.setUTCDate(startDate.getUTCDate() - bufferDays);

		let adjustedEndDate = new Date(endDate);
		adjustedEndDate.setUTCDate(endDate.getUTCDate() + bufferDays);

		// Date 객체에서 연도, 월, 일 추출
		startYear = adjustedStartDate.getUTCFullYear();
		startMonth = adjustedStartDate.getUTCMonth(); // 월은 0부터 시작
		startDay = adjustedStartDate.getUTCDate();

		endYear = adjustedEndDate.getUTCFullYear();
		endMonth = adjustedEndDate.getUTCMonth();
		endDay = adjustedEndDate.getUTCDate();

		console.log("조정된 시작일: ", adjustedStartDate.toISOString());
		console.log("조정된 종료일: ", adjustedEndDate.toISOString());

	    // 차트 컨테이너 ID 설정
	    ganttChart.container('gantt-chart'); // 컨테이너 ID를 수정하세요

	    // 차트 그리기 시작
	    ganttChart.draw();

		zoomStart = Date.UTC(startYear, startMonth, startDay);
		zoomEnd = Date.UTC(endYear, endMonth, endDay);

		console.log(`zoomStart 체크1: `, zoomStart);
		console.log(`zoomEnd 체크1: `, zoomEnd);

		// 전체 데이터를 보이도록 날짜 범위 조정
		// ex) ganttChart.zoomTo(Date.UTC(2024, 8, 1), Date.UTC(2024, 12, 30));
		ganttChart.zoomTo(zoomStart, zoomEnd);

		// 날짜 범위 조정 (scale 사용)
		timeline.scale().minimum(zoomStart);
		timeline.scale().maximum(zoomEnd);

		ganttChart.fitAll();

		ganttChartLoaded = true;

		// 진행도 업데이트 버튼 이벤트 리스너 추가
		document.getElementById('update-progress-button').addEventListener('click', updateProgress);

	  });

	  // 간트 차트 진행도 업데이트 함수
	  function updateProgress() {
	      const idElement = document.getElementById('progress-id');
		  const progressElement = document.getElementById('progress-value');
		  const id = document.getElementById('progress-id').value.trim();
	      const progressValue = document.getElementById('progress-value').value.trim();

		  // 분할기 위치 설정
		  ganttChart.splitterPosition(550); // 분할기 위치를 550px로 설정하여 고정

	      // 디버깅 로그 추가
	      console.log(`id: ${id}`);
	      console.log(`Progress Value: ${progressValue}`);
	      console.log(`Current Gantt Chart Data:`, ganttChartData);

		  // 정규 표현식을 사용하여 진행도 형식 검증
		  const progressValuePattern = /^\d{1,3}%$/;

		  // 진행도 값이 올바른 형식인지 확인
		  if (!progressValuePattern.test(progressValue)) {
			alert('진행도 값은 0%에서 100% 사이의 형식으로 입력해야 합니다. 예: 50%');

			progressElement.focus();
			progressElement.value = null;
			idElement.focus();
			idElement.value = null;

		    return; // 올바른 형식이 아닐 경우 함수 실행 중지
		  }

		  // 입력 ID가 숫자형인지 확인하고 숫자로 변환
		  const numericId = Number(id);
		  if (isNaN(numericId)) {
		    alert('ID는 유효한 숫자여야 합니다.');

			progressElement.focus();
			progressElement.value = null;
			idElement.focus();
			idElement.value = null;

		    return; // ID가 숫자가 아닐 경우 함수 실행 중지
		  }

	      if (ganttChart && id && progressValue) {
	          // 데이터 업데이트 로직
	          function findTaskById(tasks, id) {
	              console.log(`Searching for ID: ${id} in tasks`, tasks);

	              for (let task of tasks) {
	                  console.log(`Checking task ID: ${task.id}`);
	                  if (task.id == id) {
	                      console.log(`Found task with ID: ${id}`);
	                      return task;
	                  }
	                  if (task.children) {
	                      const found = findTaskById(task.children, id);
	                      if (found) return found;
	                  }
	              }
	              return null;
	          }

	          // 작업을 찾기
	          const taskToUpdate = findTaskById(ganttChartData, id);

	          if (taskToUpdate) {
	              taskToUpdate.progressValue = progressValue;

	              // Gantt 차트 데이터 업데이트
	              let updatedData = anychart.data.tree(ganttChartData, 'as-tree');
	              ganttChart.data(updatedData);

				  console.log(`zoomStart 체크2: `, zoomStart);
				  console.log(`zoomEnd 체크2: `, zoomEnd);

	              // 차트 다시 그리기
	              ganttChart.draw();

	              // 전체 데이터가 보이도록 날짜 범위 조정
				  ganttChart.zoomTo(zoomStart, zoomEnd);

	          } else {
	              alert('해당 ID의 작업을 찾을 수 없습니다.');

				  progressElement.focus();
				  progressElement.value = null;
				  idElement.focus();
				  idElement.value = null;
	          }
	      }
	  }

*/

/*
// 캘린더 로드
    function loadCalendar() {
        if (calendar) return; // 이미 캘린더가 로드된 경우

        const calendarEl = document.getElementById('calendar');
        if (!calendarEl) return;
		
        calendar = new FullCalendar.Calendar(calendarEl, {
            initialView: 'dayGridMonth',
            headerToolbar: {
                left: 'prev,next today',
                center: 'title',
                right: 'dayGridMonth,timeGridWeek,timeGridDay,listMonth'
            },
            views: {
                listMonth: {
                    buttonText: 'List'
                }
            },
            events: [
                {
                    title: '프로젝트 시작',
                    start: '2024-09-01',
                    color: 'green'
                },
                {
                    title: '중간 점검',
                    start: '2024-09-15',
                    color: 'orange'
                },
                {
                    title: '프로젝트 마감',
                    start: '2024-09-30',
                    color: 'red'
                },
                {
                    title: '미팅',
                    start: '2024-09-10T10:00:00',
                    end: '2024-09-10T12:00:00'
                },
                {
                    title: '휴가',
                    start: '2024-09-20',
                    end: '2024-09-22'
                }
            ],
            dateClick: function(info) {
                openEventModal(info.dateStr);
            },
            eventClick: function(info) {
                openEventDetailModal(info.event);
            }
        });

        calendar.render();
    }

    // 일정 추가 모달 열기
    function openEventModal(dateStr) {
		
        const eventModal = document.getElementById('event-modal');
        const eventForm = document.getElementById('event-form');

        eventModal.classList.remove('hidden');
        document.getElementById('event-start-date').value = dateStr;
        document.getElementById('event-end-date').value = dateStr;

        const closeButton = eventModal.querySelector('.btn-close');
        closeButton.addEventListener('click', () => {
            eventModal.classList.add('hidden');
        });

        // 기존의 이벤트 리스너를 제거하고 새로운 리스너를 추가
        eventForm.removeEventListener('submit', handleEventFormSubmit);
        eventForm.addEventListener('submit', handleEventFormSubmit);

    }

    // 이벤트 폼 제출 핸들러
    function handleEventFormSubmit(event) {
        event.preventDefault();
		
        const title = document.getElementById('event-title').value;
        const type = document.getElementById('event-type').value;
        const startDate = document.getElementById('event-start-date').value;
        const endDate = document.getElementById('event-end-date').value;
        const startTime = document.getElementById('event-start-time').value;
        const endTime = document.getElementById('event-end-time').value;

		console.log(`Event submitted - Title: ${title}, Type: ${type}, Start Date: ${startDate}, End Date: ${endDate}, Start Time: ${startTime}, End Time: ${endTime}`);
		
        if (title && startDate && endDate) {
            let startDateTime, endDateTime;

            // 중복 일정 체크
            const events = calendar.getEvents();
            const isDuplicate = (start, end) => {
                return events.some(event =>
                    (event.title === title && 
                    event.startStr === start && 
                    event.endStr === end)
                );
            };

            if (type === '1') {
                // 일일 일정 (반복 일정)
                const startOfDayTime = `${startDate}T${startTime}:00`;
                const endOfDayTime = `${endDate}T${endTime}:00`;

                let currentDate = new Date(startDate);
                const endDateObj = new Date(endDate);

                while (currentDate <= endDateObj) {
                    const eventStart = `${currentDate.toISOString().split('T')[0]}T${startTime}:00`;
                    const eventEnd = `${currentDate.toISOString().split('T')[0]}T${endTime}:00`;

                    if (!isDuplicate(eventStart, eventEnd)) {
                        calendar.addEvent({
                            title: title,
                            start: eventStart,
                            end: eventEnd
                        });
                    }
                    currentDate.setDate(currentDate.getDate() + 1);
                }
            } else if (type === '2') {
                // 시간 기반 일정
                startDateTime = `${startDate}T${startTime}:00`;
                endDateTime = `${endDate}T${endTime}:00`;

                if (!isDuplicate(startDateTime, endDateTime)) {
                    calendar.addEvent({
                        title: title,
                        start: startDateTime,
                        end: endDateTime
                    });
                }
            }

            // 모달 닫기 및 폼 리셋
            document.getElementById('event-modal').classList.add('hidden');
            document.getElementById('event-form').reset();
        }
    }

    // 모달 내용 초기화
    function resetEventForm() {
        document.getElementById('event-title').value = '';
        document.getElementById('event-type').value = '1';
        document.getElementById('event-start-date').value = '';
        document.getElementById('event-end-date').value = '';
        document.getElementById('event-start-time').value = '';
        document.getElementById('event-end-time').value = '';
    }

    // 일정 상세 모달 열기
    function openEventDetailModal(event) {
        const modal = document.createElement('div');
        modal.classList.add('modal');

        const modalContent = document.createElement('div');
        modalContent.classList.add('modal-content');

        const title = document.createElement('h3');
        title.textContent = event.title;

        const details = document.createElement('p');
        details.textContent = formatEventDetails(event);

        const closeButton = document.createElement('button');
        closeButton.textContent = 'Close';
        closeButton.classList.add('btn-close');
        closeButton.addEventListener('click', () => {
            document.body.removeChild(modal);
        });

        const deleteButton = document.createElement('button');
        deleteButton.textContent = 'Delete';
        deleteButton.classList.add('btn-delete');
        deleteButton.addEventListener('click', () => {
            event.remove();
            document.body.removeChild(modal);
        });

        const buttonsContainer = document.createElement('div');
        buttonsContainer.classList.add('modal-buttons');
        buttonsContainer.appendChild(closeButton);
        buttonsContainer.appendChild(deleteButton);

        modalContent.appendChild(title);
        modalContent.appendChild(details);
        modalContent.appendChild(buttonsContainer);
        modal.appendChild(modalContent);

        document.body.appendChild(modal);
    }

    // 일정 상세 내용 포맷팅
    function formatEventDetails(event) {
        const startDate = new Date(event.start);
        const endDate = event.end ? new Date(event.end) : null;
        const startDateStr = formatDate(startDate);
        const endDateStr = endDate ? formatDate(endDate) : 'No end time';
        return `${startDateStr} ~ ${endDateStr}`;
    }

    // 날짜 포맷팅
    function formatDate(date) {
        const options = {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            hour12: true
        };
        return new Intl.DateTimeFormat('en-US', options).format(date);
    }
*/


function loadApplicationList() {
	const projectNum = getQueryParam('projectNum');
    fetch(`/project/application-list?projectNum=${projectNum}`)  // 서버에서 클라이언트의 지원자 목록을 가져옴
        .then(response => response.json())
        .then(applications => {
            let contentHtml = `
            <h3>지원자 목록</h3>
            <table class="application-table">
                <thead>
                    <tr>
                        <th>신청자 ID</th>
                        <th>프로젝트명</th>
                        <th>상태</th>
                        <th>동작</th>
                    </tr>
                </thead>
                <tbody>
            `;

			const pendingApplications = applications.filter(application => application.applicationResult === 'PENDING');

			pendingApplications.forEach(application => {
				let statusText = '지원 신청 중';

				// 동작 버튼은 항상 표시
				let actionHtml = `
                    <button class="btn-approve" onclick="updateApplicationStatus(${application.projectNum}, '${application.freelancerId}', 'ACCEPTED')">찬성</button>
                    <button class="btn-reject" onclick="updateApplicationStatus(${application.projectNum}, '${application.freelancerId}', 'REJECTED')">반대</button>
                `;

				contentHtml += `
                <tr>
                    <td>${application.freelancerId}</td>
                    <td>${application.projectTitle}</td>
                    <td>${statusText}</td> <!-- 상태란에 지원 신청 중 표시 -->
                    <td>${actionHtml}</td> <!-- 동작란에 찬성/반대 버튼 표시 -->
                </tr>
                `;
			});

			contentHtml += `</tbody></table>`;

			// PENDING 상태의 지원자가 없다면 빈 목록 메시지 표시
			if (pendingApplications.length === 0) {
				contentHtml += `<p class="empty-list">현재 지원 신청 중인 프리랜서가 없습니다.</p>`;
			}

			document.getElementById('application-content').innerHTML = contentHtml;
		})
		.catch(error => {
			console.error('Error fetching applications:', error);
		});
}

function updateApplicationStatus(projectNum, freelancerId, status) {
    fetch('/project/update-application-status', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `projectNum=${projectNum}&freelancerId=${freelancerId}&status=${status}`
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('상태 업데이트에 실패했습니다.');
            }
            return response.text();
        })
        .then(data => {
            alert('상태가 성공적으로 업데이트되었습니다.');
            loadApplicationList();  // 상태 업데이트 후 목록 새로고침
        })
        .catch(error => {
            console.error('Error updating application status:', error);
            alert('상태 업데이트 중 오류가 발생했습니다.');
        })
        .finally(() => {
            isUpdating = false;  // 업데이트 완료 후 플래그 해제
        });
}

function getQueryParam(param) {
	const urlParams = new URLSearchParams(window.location.search);  // URL에서 쿼리 스트링을 파싱
	return urlParams.get(param);  // 특정 파라미터 값 가져오기
}

function loadTeamList() {
    const projectNum = getQueryParam('projectNum'); // URL에서 projectNum 가져오기
    if (!projectNum) {
        alert('프로젝트 번호를 찾을 수 없습니다.');
        return;
    }

    fetch(`/project/team-list?projectNum=${projectNum}`)
        .then(response => response.json())
        .then(teamList => {
            let contentHtml = `<h3>팀원 목록</h3>`;
            if (teamList.length > 0) {
                contentHtml += `
                <table class="team-table">
                    <thead>
                        <tr>
                            <th>팀원 ID</th>
                            <th>역할</th>
                            <th>상태</th>
                        </tr>
                    </thead>
                    <tbody>
                `;

                teamList.forEach(member => {
                    contentHtml += `
                    <tr>
                        <td>${member.memberId}</td>
                        <td>${member.roleName === 'ROLE_CLIENT' ? '클라이언트' : '프리랜서'}</td> <!-- 역할 정보가 있으면 표시, 없으면 '팀원' -->
                        <td>${member.status || '활동 중'}</td> <!-- 상태 정보가 있으면 표시, 없으면 '활동 중' -->
                    </tr>
                    `;
                });

                contentHtml += `</tbody></table>`;
            } else {
                contentHtml += `<p class="empty-list">팀원이 없습니다.</p>`;
            }

            document.getElementById('team-content').innerHTML = contentHtml;
        })
        .catch(error => {
            console.error('Error fetching team list:', error);
        });
}