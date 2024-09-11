document.addEventListener('DOMContentLoaded', function() {
    const tabs = document.querySelectorAll('.tab-link');
    const contents = document.querySelectorAll('.tab-content');
    let calendar = null;
    let ganttChartLoaded = false;

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
                loadGanttChart();
            }
        });
    });

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
        const title = prompt('새 일정 제목을 입력하세요:', '');
        if (!title) return;

        const durationType = prompt('일정 입력 방식을 선택하세요. "1"은 반복 일정을 생성하고, "2"는 하나의 기간으로 표시합니다:', '1');
        const startDate = prompt('일정 시작 날짜를 입력하세요 (예: 2024-09-10):', dateStr);
        const endDate = prompt('일정 종료 날짜를 입력하세요 (예: 2024-09-12):', startDate);

        const startTime = prompt('일정 시작 시간을 입력하세요 (예: 09:00):', '09:00');
        const endTime = prompt('일정 종료 시간을 입력하세요 (예: 17:00):', '17:00');

        if (startDate && endDate && startTime && endTime) {
            if (durationType === '1') {
                const startDateTime = `${startDate}T${startTime}:00`;
                const endDateTime = `${endDate}T${endTime}:00`;

                const start = new Date(startDate);
                const end = new Date(endDate);

                let currentDate = start;
                while (currentDate <= end) {
                    const eventStart = new Date(currentDate);
                    const eventEnd = new Date(currentDate);
                    eventStart.setHours(new Date(startDateTime).getHours(), new Date(startDateTime).getMinutes());
                    eventEnd.setHours(new Date(endDateTime).getHours(), new Date(endDateTime).getMinutes());

                    calendar.addEvent({
                        title: title,
                        start: eventStart.toISOString(),
                        end: eventEnd.toISOString()
                    });

                    currentDate.setDate(currentDate.getDate() + 1);
                }
            } else if (durationType === '2') {
                const startDateTime = `${startDate}T${startTime}:00`;
                const endDateTime = `${endDate}T${endTime}:00`;

                calendar.addEvent({
                    title: title,
                    start: startDateTime,
                    end: endDateTime
                });
            }
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

    // 간트차트 로드
    function loadGanttChart() {
        const ganttChartEl = document.getElementById('gantt-chart');
        if (!ganttChartEl) return;

        if (ganttChartLoaded) return;

        ganttChartLoaded = true;

        anychart.onDocumentReady(function() {
            const data = [
                {
                    "id": "1",
                    "name": "프로젝트 시작",
                    "progressValue": "30%",
                    "actualStart": "2024-09-01",
                    "actualEnd": "2024-09-08",
                    "connectTo": "5",
                    "connectorType": "FinishStart",
                    "children": []
                },
                {
                    "id": "2",
                    "name": "중간 점검",
                    "actualStart": "2024-09-15",
                    "actualEnd": "2024-09-15",
                    "connectorType": "FinishStart",
                    "children": []
                },
                {
                    "id": "3",
                    "name": "프로젝트 마감",
                    "actualStart": "2024-09-30",
                    "actualEnd": "2024-09-30",
                    "connectorType": "FinishStart",
                    "children": []
                },
                {
                    "id": "4",
                    "name": "미팅",
                    "actualStart": "2024-09-10T10:00:00",
                    "actualEnd": "2024-09-10T12:00:00",
                    "connectorType": "FinishStart",
                    "children": []
                },
                {
                    "id": "5",
                    "name": "휴가",
                    "progressValue": "80%",
                    "actualStart": "2024-09-20",
                    "actualEnd": "2024-09-22",
                    "connectorType": "FinishStart",
                    "children": []
                }
            ];

            let chart = anychart.ganttProject();

            let treeData = anychart.data.tree(data, 'as-table');
            chart.data(treeData);

            chart.getTimeline().tasks().fill('#00bcd4');
            chart.getTimeline().tasks().stroke(null);

            chart.getTimeline().tasks().progress(function() {
                return this.getData('progress') || 0;
            });

            let dataGrid = chart.dataGrid();

            dataGrid.column(0)
                .title('#')
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

            chart.container('gantt-chart');
            chart.draw();

            chart.zoomTo(Date.UTC(2024, 8, 1), Date.UTC(2024, 9, 30));
        });
    }
});
