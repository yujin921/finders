document.addEventListener('DOMContentLoaded', function() {
    const tabs = document.querySelectorAll('.tab-link');
    const contents = document.querySelectorAll('.tab-content');

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
            anychart.data.loadJsonFile(
                'https://cdn.anychart.com/samples/gantt-charts/activity-oriented-chart/data.json',
                function (data) {
                    let treeData = anychart.data.tree(data, 'as-table');

                    let chart = anychart.ganttProject();
                    chart.data(treeData);

                    chart.splitterPosition(370);

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
                        .width(80)
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

                    chart.zoomTo(951350400000, 954201600000);
                }
            );
        });
    }
});
