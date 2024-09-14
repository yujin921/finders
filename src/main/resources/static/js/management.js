document.addEventListener('DOMContentLoaded', function() {
    const tabs = document.querySelectorAll('.tab-link');
    const contents = document.querySelectorAll('.tab-content');
    let calendar = null;
	let ganttChart = null; // Gantt 차트 인스턴스를 저장할 변수
	let ganttChartData = []; // Gantt 차트 데이터 저장 변수
	let ganttChartLoaded = false;

	// 간트차트 화면 상 표시 날짜 범위 지정을 위해 필요한 변수들
	let startDateStr = null;
	let endDateStr = null;
	let startDate = null;
	let endDate = null;
	let startYear = null;
	let startMonth = null;
	let startDay = null;
	let endYear = null;
	let endMonth = null;
	let endDay = null;

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
            } else if (targetId === 'application-content') {
                loadApplicationList();
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

            ganttChart.container('gantt-chart');
            ganttChart.draw();

			// 전체 데이터 표시
			ganttChart.fitAll();

			// ex) ganttChart.zoomTo(Date.UTC(2024, 8, 1), Date.UTC(2024, 9, 30));
			ganttChart.zoomTo(Date.UTC(startYear, startMonth, startDay), Date.UTC(endYear, endMonth, endDay));

            ganttChartLoaded = true;

            // 진행도 업데이트 버튼 이벤트 리스너 추가
            document.getElementById('update-progress-button').addEventListener('click', updateProgress);
        });
    }

	// 간트 차트 진행도 업데이트 함수
    function updateProgress() {
        const id = document.getElementById('progress-id').value.trim();
        const progressValue = document.getElementById('progress-value').value.trim();

        if (ganttChart && id && progressValue) {
            // 하위 작업을 포함하여 작업을 찾기 위한 재귀 함수
            function findTaskById(tasks, id) {
                for (let task of tasks) {
                    if (task.id === id) {
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

                // Gantt 차트 다시 그림
                ganttChart.draw();

                ganttChart.zoomTo(Date.UTC(startYear, startMonth, startDay), Date.UTC(endYear, endMonth, endDay));

                console.log(`작업 ID ${id}의 진행도가 ${progressValue}로 업데이트되었습니다.`);
            } else {
                alert('해당 ID의 작업을 찾을 수 없습니다.');
            }
        } else {
            alert('올바른 작업 ID와 진행도 값을 입력해 주세요.');
        }
    }
});

function loadApplicationList() {
    fetch('/project/application-list')  // 서버에서 클라이언트의 지원자 목록을 가져옴
        .then(response => response.json())
        .then(applications => {
            let contentHtml = `
            <h3>지원자 목록</h3>
            <table>
                <thead>
                    <tr>
                        <th>프리랜서 ID</th>
                        <th>게시물 제목</th>
                        <th>상태</th>
                        <th>동작</th>
                    </tr>
                </thead>
                <tbody>
            `;

            applications.forEach(application => {
                let statusText = application.applicationResult;

                if (application.applicationResult === 'PENDING') {
                    // PENDING일 경우 상태에 "지원 신청 중" 표시
                    statusText = '지원 신청 중';
                }

                // 동작 버튼은 항상 표시
                let actionHtml = `
                    <button onclick="updateApplicationStatus(${application.projectNum}, '${application.freelancerId}', 'ACCEPTED')">찬성</button>
                    <button onclick="updateApplicationStatus(${application.projectNum}, '${application.freelancerId}', 'REJECTED')">반대</button>
                `;

                contentHtml += `
                <tr>
                    <td>${application.freelancerId}</td>
                    <td>${application.projectTitle}</td>
                    <td>${statusText}</td> <!-- 상태란에 지원 신청 중 또는 다른 상태 표시 -->
                    <td>${actionHtml}</td> <!-- 동작란에 찬성/반대 버튼 표시 -->
                </tr>
                `;
            });

            contentHtml += `</tbody></table>`;
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