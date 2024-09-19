window.addEventListener('load', adjustSidebarHeight);
window.addEventListener('resize', adjustSidebarHeight);

function adjustSidebarHeight() {
    const container = document.querySelector('.container');
    const sidebar = document.querySelector('.sidebar');
    const mainContent = document.querySelector('.main-content');

    // 사이드바의 높이를 전체 컨테이너 높이에서 main-content의 높이를 뺀 값으로 설정
    sidebar.style.height = `${container.clientHeight}px`;
}

document.addEventListener('DOMContentLoaded', function() {
    const tabs = document.querySelectorAll('.tab-link');
    const contents = document.querySelectorAll('.tab-content');
    let calendar;
	let ganttChart; // Gantt 차트 인스턴스를 저장할 변수
	// let ganttChartData = []; // Gantt 차트 데이터 저장 변수
	let timeline;
	let ganttChartLoaded = false;
	
	// 간트차트 화면 상 표시 날짜 범위 지정을 위해 필요한 변수들
	let startDateStr = null;
	let endDateStr = null;
	let startDate = null;
	let endDate = null;
	let zoomStart;
	let zoomEnd;

    tabs.forEach(tab => {
        tab.addEventListener('click', function(event) {
            event.preventDefault();
            const targetId = this.getAttribute('data-tab');

            contents.forEach(content => {
                content.classList.remove('active');
            });

            document.getElementById(targetId).classList.add('active');

            if (targetId === 'calendar-content') {
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
	
	// 간트차트 데이터 로드
	function importGanttChartData() {
		
		// 페이지 로드 시 자동으로 데이터 로드 및 차트 생성
		$.ajax({
		    url: 'getGanttChartData?projectNum=' + projectNum, // URL 쿼리 파라미터로 전송
		    type: 'GET',
		    dataType: 'json',
		    success: function(response) {
				
				// 응답 데이터 검증
	            if (response && response.data && Array.isArray(response.data) && response.data.length > 0) {

	                // 데이터 추출
	                const data = response.data;
	                
	                // 가장 이른 시작일 (baselineStart 또는 actualStart 중 가장 빠른 날짜 찾기)
	                const earliestStart = data
	                    .map(item => new Date(item.baselineStart || item.actualStart))
	                    .filter(date => !isNaN(date.getTime()))
	                    .reduce((earliest, current) => current < earliest ? current : earliest, new Date(8640000000000000));
	                
	                // 가장 늦은 종료일 (baselineEnd 또는 actualEnd 중 가장 늦은 날짜 찾기)
	                const latestEnd = data
	                    .map(item => new Date(item.baselineEnd || item.actualEnd))
	                    .filter(date => !isNaN(date.getTime()))
	                    .reduce((latest, current) => current > latest ? current : latest, new Date(-8640000000000000));
	                
	                // 여유 기간 추가
	                const bufferDay = 30;
	                const adjustedStartDate = new Date(earliestStart.getTime() - bufferDay * 24 * 60 * 60 * 1000);
	                const adjustedEndDate = new Date(latestEnd.getTime() + bufferDay * 24 * 60 * 60 * 1000);

	                // 날짜 포맷 설정
	                const formatter = new Intl.DateTimeFormat('en-GB', { year: 'numeric', month: '2-digit', day: '2-digit' });
	                const format = (date) => date ? formatter.format(date).replace(/\//g, '-') + 'T00:00:00Z' : null;

	                // 조정된 날짜를 로그로 출력 (디버깅 용)
	                console.log('조정된 시작일:', format(adjustedStartDate));
	                console.log('조정된 종료일:', format(adjustedEndDate));

	                // 간트차트 생성 함수 호출
	                createGanttChart(data, format(adjustedStartDate), format(adjustedEndDate));
	            } else if (!response.data) {
					alert('간트 차트 데이터를 로드하는 데 필요한 정보가 없습니다.n\먼저 업무 등록을 해주세요.');
					console.error('데이터 없음:', response);
				}else {
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
	function createGanttChart(ganttChartData, adjustedStartDate, adjustedEndDate) {
		
		console.log('createGanttChart 호출:');
	    console.log('간트차트 데이터:', ganttChartData); // 데이터 확인 로그
	    console.log('조정된 시작일:', adjustedStartDate);
	    console.log('조정된 종료일:', adjustedEndDate);
		
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

			// 날짜 형식 로그 추가
	        ganttChartData.forEach(task => {
	            console.log('Task Data 체크용:', task);
	            console.log('Actual Start 체크용:', task.actualStart);
	            console.log('Actual End 체크용:', task.actualEnd);
	        });
			
	        // Gantt 데이터에서 시작일과 종료일 추출
	        let chartFirstElement = ganttChartData[0];
	        let chartLastElement = ganttChartData[ganttChartData.length - 1];

	        console.log("시작일 형식: ", chartFirstElement.actualStart);
	        console.log("종료일 형식: ", chartLastElement.children[chartLastElement.children.length - 1].actualEnd);

	        // 날짜 문자열을 Date 객체로 변환 (가정: 날짜 문자열은 "YYYY-MM-DD" 형식)
	        startDateStr = chartFirstElement.actualStart;
	        endDateStr = chartLastElement.children[chartLastElement.children.length - 1].actualEnd;

	        // 날짜 문자열을 Date 객체로 변환합니다
	        startDate = new Date(startDateStr);
	        endDate = new Date(endDateStr);

	        // 유효한 날짜인지 확인
	        if (isNaN(startDate.getTime())) {
	            console.warn('유효하지 않은 시작일:', startDateStr);
	            startDate = new Date(); // 기본값을 현재 날짜로 설정
	        }
	        if (isNaN(endDate.getTime())) {
	            console.warn('유효하지 않은 종료일:', endDateStr);
	            endDate = new Date(); // 기본값을 현재 날짜로 설정
	        }

	        // 여유 기간을 설정합니다
	        const bufferDays = 30;

	        // 날짜 문자열을 Date 객체로 변환
	        let adjustedStartDateObj = new Date(adjustedStartDate);
	        let adjustedEndDateObj = new Date(adjustedEndDate);

	        // 날짜가 유효한지 확인
	        if (isNaN(adjustedStartDateObj.getTime())) {
	            console.warn('유효하지 않은 조정된 시작일:', adjustedStartDate);
	            adjustedStartDateObj = startDate;
	        }
	        if (isNaN(adjustedEndDateObj.getTime())) {
	            console.warn('유효하지 않은 조정된 종료일:', adjustedEndDate);
	            adjustedEndDateObj = endDate;
	        }

	        // 여유 기간을 추가합니다
	        adjustedStartDateObj.setUTCDate(adjustedStartDateObj.getUTCDate() - bufferDays);
	        adjustedEndDateObj.setUTCDate(adjustedEndDateObj.getUTCDate() + bufferDays);

	        console.log("조정된 시작일:", adjustedStartDateObj.toISOString());
	        console.log("조정된 종료일:", adjustedEndDateObj.toISOString());

	        // Gantt 차트를 HTML 컨테이너에 렌더링합니다
	        ganttChart.container('gantt-chart'); // 컨테이너 ID를 수정하세요
	        ganttChart.draw();

	        // 날짜가 올바른지 확인하고 순서를 맞춤
	        zoomStart = Math.min(adjustedStartDateObj.getTime(), adjustedEndDateObj.getTime());
	        zoomEnd = Math.max(adjustedStartDateObj.getTime(), adjustedEndDateObj.getTime());

	        console.log("Zoom Start:", zoomStart);
	        console.log("Zoom End:", zoomEnd);

	        // 차트의 날짜 범위를 조정합니다
	        ganttChart.zoomTo(zoomStart, zoomEnd);
	        timeline.scale().minimum(zoomStart);
	        timeline.scale().maximum(zoomEnd);

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
		    let progressValue = $('#progress-value-input').val();

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
						$('#progress-value-input').val('');
		            
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

		    // AJAX 요청을 통해 서버에 실제 일정 업데이트 요청
		    $.ajax({
		        url: 'updateSchedule',
		        type: 'POST',
		        data: {
		            entityType: entityType,
		            id: ganttId,
		            actualStart: actualStart,
		            actualEnd: actualEnd
		        },
		        success: function(response) {
					console.log('업데이트 응답:', response); // 응답 로그 추가
				    
		            if (response === 'success') {
		                alert('실제 일정이 업데이트되었습니다.');

		                // 간트 차트를 새로고침하여 반영된 일정을 확인
		                refreshGanttChart();
						
						// 입력 박스와 select 박스 값을 초기화
						$('#actual-start-date').val('');
						$('#actual-end-date').val('');
		                
		            } else {
		                alert('실제 일정 업데이트에 실패했습니다.');
		            }
		        },
		        error: function(xhr, status, error) {
		            console.error('AJAX Error:', status, error);
		            alert('서버 요청 중 오류가 발생했습니다.');
		        }
		    });
		}

		// 간트차트 새로고침 함수
		function refreshGanttChart() {
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
		            if (response && response.data && Array.isArray(response.data) && response.data.length > 0 &&
		                response.adjustedStartDate && response.adjustedEndDate) {

		                // 데이터 추출
		                const data = response.data;
		                
		                // 가장 이른 시작일 (baselineStart 또는 actualStart 중 가장 빠른 날짜 찾기)
		                const earliestStart = data
		                    .map(item => new Date(item.baselineStart || item.actualStart))
		                    .filter(date => !isNaN(date.getTime()))
		                    .reduce((earliest, current) => current < earliest ? current : earliest, new Date(8640000000000000));
		                
		                // 가장 늦은 종료일 (baselineEnd 또는 actualEnd 중 가장 늦은 날짜 찾기)
		                const latestEnd = data
		                    .map(item => new Date(item.baselineEnd || item.actualEnd))
		                    .filter(date => !isNaN(date.getTime()))
		                    .reduce((latest, current) => current > latest ? current : latest, new Date(-8640000000000000));
		                
		                // 여유 기간 추가
		                const bufferDay = 30;
		                const adjustedStartDate = new Date(earliestStart.getTime() - bufferDay * 24 * 60 * 60 * 1000);
		                const adjustedEndDate = new Date(latestEnd.getTime() + bufferDay * 24 * 60 * 60 * 1000);

		                // 날짜 포맷 설정
		                const formatter = new Intl.DateTimeFormat('en-GB', { year: 'numeric', month: '2-digit', day: '2-digit' });
		                const format = (date) => date ? formatter.format(date).replace(/\//g, '-') + 'T00:00:00Z' : null;

		                // 조정된 날짜를 로그로 출력 (디버깅 용)
		                console.log('조정된 시작일:', format(adjustedStartDate));
		                console.log('조정된 종료일:', format(adjustedEndDate));

		                // 간트차트 생성 함수 호출
		                createGanttChart(data, format(adjustedStartDate), format(adjustedEndDate));
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
*/


/*
	// 간트 차트 로드
    function loadGanttChart() {
        if (ganttChartLoaded) return; // 이미 간트 차트가 로드된 경우

        anychart.onDocumentReady(function() {
            // Gantt 차트 데이터 예시
            ganttChartData = [
                { id: '1', name: '디자인', actualStart: '2024-09-01', actualEnd: '2024-09-04', progressValue: "0%" },
                { id: '2', name: '기획', actualStart: '2024-09-05', actualEnd: '2024-09-10', progressValue: "0%" },            
				{
				    id: "3",
				    name: "개발",
				    baselineStart: "2024-09-11",
				    baselineEnd: "2024-09-14",
				    actualStart: "2024-09-15",
				    actualEnd: "2024-09-20",
					progressValue: "0%",
				    children: [
				      {
				        id: "3_1",
				        name: "Analysis",
				        baselineStart: "2024-09-15",
				        baselineEnd: "2024-09-16",
				        actualStart: "2024-09-15",
				        actualEnd: "2024-09-17",
						progressValue: "0%"
				      },
				      {
				        id: "3_2",
				        name: "Design",
				        baselineStart: "2024-09-17",
				        baselineEnd: "2024-09-18",
				        actualStart: "2024-09-16",
				        actualEnd: "2024-09-17",
						progressValue: "0%"
				      },
				      {
				        id: "3_3",
				        name: "Implementation",
				        baselineStart: "2024-09-18",
				        baselineEnd: "2024-09-19",
				        actualStart: "2024-09-17",
				        actualEnd: "2024-09-18",
						progressValue: "0%"
				      }
				  ]},
				  { id: '4', name: '테스트', actualStart: '2024-09-19', actualEnd: '2024-09-30', progressValue: "0%" }
			];

            ganttChart = anychart.ganttProject();
            
			// treeData 변수 정의
			let treeData = anychart.data.tree(ganttChartData, 'as-tree');
			ganttChart.data(treeData);

            ganttChart.getTimeline().tasks().fill('#00bcd4');
            ganttChart.getTimeline().tasks().stroke(null);

            ganttChart.getTimeline().tasks().progress(function() {
                return this.getData('progress') || 0;
            });

            let dataGrid = ganttChart.dataGrid();

            dataGrid.column(0)
                .title('ID')
                .width(30)
                .labels({ hAlign: 'center' });

            dataGrid.column(1).labels().hAlign('left').width(180);

            dataGrid.column(2)
                .title('Start Time')
                .width(70)
                .labels()
                .hAlign('right')
                .format(function () {
                    let date = new Date(this.actualStart);
                    let month = date.getUTCMonth() + 1;
                    let strMonth = month > 9 ? month : '0' + month;
                    let utcDate = date.getUTCDate();
                    let strDate = utcDate > 9 ? utcDate : '0' + utcDate;
                    return date.getUTCFullYear() + '.' + strMonth + '.' + strDate;
                });

            dataGrid.column(3)
                .title('End Time')
                .width(70)
                .labels()
                .hAlign('right')
                .format(function () {
                    let date = new Date(this.actualEnd);
                    let month = date.getUTCMonth() + 1;
                    let strMonth = month > 9 ? month : '0' + month;
                    let utcDate = date.getUTCDate();
                    let strDate = utcDate > 9 ? utcDate : '0' + utcDate;
                    return date.getUTCFullYear() + '.' + strMonth + '.' + strDate;
                });
				
			console.log("시작일 형식: ", ganttChartData[0].actualStart);
			console.log("종료일 형식: ", ganttChartData[ganttChartData.length - 1].actualEnd);

			startDateStr = ganttChartData[0].actualStart;
			endDateStr = ganttChartData[ganttChartData.length - 1].actualEnd;
			
			// 날짜 문자열을 Date 객체로 변환 (가정: 날짜 문자열은 "YYYY-MM-DD" 형식)
			startDate = new Date(startDateStr);
			endDate = new Date(endDateStr);
			
			// Date 객체에서 연도, 월, 일 추출
			startYear = startDate.getUTCFullYear();
			startMonth = startDate.getUTCMonth(); // 월은 0부터 시작
			startDay = startDate.getUTCDate();

			endYear = endDate.getUTCFullYear();
			endMonth = endDate.getUTCMonth();
			endDay = endDate.getUTCDate();
			
			// 차트가 컨테이너에 맞게 크기를 조정
	        ganttChart.container('gantt-chart');
	        ganttChart.width("100%");  // 가로 크기를 컨테이너에 맞추기
	        ganttChart.height(600);    // 높이는 고정
	        ganttChart.draw();

	        // 전체 데이터 표시
	        ganttChart.fitAll();
			ganttChartLoaded = true;

			// 특정 날짜 범위로 확대할 경우 사용
			// ex) ganttChart.zoomTo(Date.UTC(2024, 8, 1), Date.UTC(2024, 12, 30));
			// ganttChart.zoomTo(Date.UTC(startYear, startMonth, startDay), Date.UTC(endYear, endMonth, endDay));

            // ganttChartLoaded = true;

            // 진행도 업데이트 버튼 이벤트 리스너 추가
            document.getElementById('update-progress-button').addEventListener('click', updateProgress);
        });
    }

	// 간트 차트 진행도 업데이트 함수
  function updateProgress() {
    const id = document.getElementById('progress-id').value.trim();
    const progressValue = document.getElementById('progress-value').value.trim();

    // 디버깅 로그 추가
    console.log(`id: ${id}`);
    console.log(`Progress Value: ${progressValue}`);
    console.log(`Current Gantt Chart Data:`, ganttChartData);

    if (id && progressValue) {
      // 데이터 업데이트 로직
      function updateTaskProgress(tasks, id, progressValue) {
        console.log(`Searching for ID: ${id} in tasks`, tasks);

        for (let task of tasks) {
          console.log(`Checking task ID: ${task.id}`);
          if (task.id === id) {
            console.log(`Found task with ID: ${id}`);
            task.progressValue = progressValue;
            return true; // 업데이트 성공
          }
          if (task.children) {
            const found = updateTaskProgress(task.children, id, progressValue);
            if (found) return true;
          }
        }
        return false; // 업데이트 실패
      }

      // 진행도 값을 업데이트
      const updated = updateTaskProgress(ganttChartData, id, progressValue);

      console.log(`Update successful: ${updated}`);

	  if (updated) {
        try {
          // Gantt 차트 데이터 업데이트
          ganttChart.data(anychart.data.tree(ganttChartData, 'as-tree'));

          // Gantt 차트 그리기 전에 타임라인과 관련된 설정을 확인
          const timeline = ganttChart.getTimeline();
          if (timeline) {
			// 현재 줌 및 레이아웃 설정을 저장
            const zoomStart = ganttChart.getTimeline().scale().minimum();
            const zoomEnd = ganttChart.getTimeline().scale().maximum();

            // Gantt 차트 데이터 업데이트
            ganttChart.data(anychart.data.tree(ganttChartData, 'as-tree'));

			// 차트 다시 그리기
			ganttChart.draw(false);

			ganttChart.zoomTo(Date.UTC(startYear, startMonth, startDay), Date.UTC(endYear, endMonth, endDay));

			// 저장된 줌 및 레이아웃 설정 복원
			ganttChart.getTimeline().scale().minimum(zoomStart);
			ganttChart.getTimeline().scale().maximum(zoomEnd);

          } else {
            console.log("Timeline 설정을 가져오는 데 실패했습니다.");
          }

        } catch (error) {
          console.error("차트를 그리던 중 오류가 발생했습니다: ", error);
        }
      } else {
        alert('해당 작업을 찾을 수 없습니다.');
      }
    } else {
      alert('올바른 작업 ID와 진행도 값을 입력해 주세요.');
    }
  }
  
  // Entity Type select box 변경 시 처리
  		$('#entityType-select').change(function() {
  		    const entityType = $(this).val();
  		    const projectNum = getQueryParam('projectNum'); // URL 쿼리 스트링에서 projectNum을 가져옴

  			console.log(`Entity Type Selected: ${entityType}`); // 디버깅용 로그
  			console.log(`Project Num: ${projectNum}`); // 디버깅용 로그
  			
  		    if (entityType && projectNum) {
  		        // 엔티티 유형이 선택되면, 해당 엔티티에 맞는 항목을 로드
  		        loadNames(projectNum, entityType);
  		        $('#name-select').prop('disabled', false);
  				$('#name-selectDate').prop('disabled', false)
  		    } else {
  		        // 엔티티 유형이 선택되지 않으면, 'Name' select box 비활성화
  		        $('#name-select').prop('disabled', true);
  		        $('#name-select').empty().append('<option value="">선택</option>');
  				$('#name-selectDate').prop('disabled', true)
  				$('#name-selectDate').empty().append('<option value="">선택</option>');
  		    }
  		});
	*/

function loadApplicationList() {
    fetch('/project/application-list')  // 서버에서 클라이언트의 지원자 목록을 가져옴
        .then(response => response.json())
        .then(applications => {
            let contentHtml = `
            <h3>지원자 목록</h3>
            <table>
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
                    <button onclick="updateApplicationStatus(${application.projectNum}, '${application.freelancerId}', 'ACCEPTED')">찬성</button>
                    <button onclick="updateApplicationStatus(${application.projectNum}, '${application.freelancerId}', 'REJECTED')">반대</button>
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
				contentHtml += `<p>현재 지원 신청 중인 프리랜서가 없습니다.</p>`;
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
                <table>
                    <thead>
                        <tr>
                            <th>팀원 ID</th>
                        </tr>
                    </thead>
                    <tbody>
                `;

				teamList.forEach(member => {
					contentHtml += `
                    <tr>
                        <td>${member.memberId}</td>
                    </tr>
                    `;
				});

				contentHtml += `</tbody></table>`;
			} else {
				contentHtml += `<p>팀원이 없습니다.</p>`;
			}

			document.getElementById('team-content').innerHTML = contentHtml;
		})
		.catch(error => {
			console.error('Error fetching team list:', error);
		});
}