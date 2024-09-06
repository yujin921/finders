document.addEventListener('DOMContentLoaded', function() {
    const tabs = document.querySelectorAll('.menu-tabs a');
    const contents = document.querySelectorAll('.tab-content');

    tabs.forEach(tab => {
        tab.addEventListener('click', function(event) {
            event.preventDefault();
            const targetId = this.id.replace('-tab', '-content');

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

    function loadCalendar() {
        const calendarEl = document.getElementById('calendar');
        if (!calendarEl) {
            return;
        }
        const calendar = new FullCalendar.Calendar(calendarEl, {
            initialView: 'dayGridMonth',
            headerToolbar: {
                left: 'prev,next today',
                center: 'title',
                right: 'dayGridMonth,timeGridWeek,timeGridDay'
            },
            events: [
                // 중요한 일정 예시
                {
                    title: '프로젝트 시작',
                    start: '2024-09-01',
                    color: 'green' // 색상 설정
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
                // 추가적인 이벤트 예시
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
            ]
        });
        calendar.render();
    }

    function loadGanttChart() {
        const ganttChartEl = document.getElementById('gantt-chart');
        if (!ganttChartEl) {
            return;
        }

        anychart.onDocumentReady(function() {
            // 프로젝트 데이터 예시
            const data = [
                {
                    id: "1",
                    name: "프로젝트 기획",
                    actualStart: "2024-09-01",
                    actualEnd: "2024-09-07",
                    progress: 100
                },
                {
                    id: "2",
                    name: "디자인 단계",
                    actualStart: "2024-09-08",
                    actualEnd: "2024-09-14",
                    progress: 70
                },
                {
                    id: "3",
                    name: "개발 단계",
                    actualStart: "2024-09-15",
                    actualEnd: "2024-10-05",
                    progress: 30
                },
                {
                    id: "4",
                    name: "테스트 단계",
                    actualStart: "2024-10-06",
                    actualEnd: "2024-10-12",
                    progress: 0
                },
                {
                    id: "5",
                    name: "배포 및 리뷰",
                    actualStart: "2024-10-13",
                    actualEnd: "2024-10-20",
                    progress: 0
                }
            ];

            // 간트 차트 생성
            const chart = anychart.ganttProject();

            // 데이터 설정
            chart.data(data);

            // 차트 제목 설정
            chart.title("프로젝트 간트 차트");

            // 차트 설정
            chart.getTimeline().setDateFormat("yyyy-MM-dd");
            chart.getTimeline().header().title().text("간트 차트");

            // 컨테이너 ID 설정
            chart.container("gantt-chart");

            // 차트 그리기
            chart.draw();
        });
    }
});
