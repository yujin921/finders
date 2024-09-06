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
});
