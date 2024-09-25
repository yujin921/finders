	$(document).ready(function() {
    list();
});

function list() {
    $.ajax({
        url: 'projectList',
        type: 'get',
        success: function(response) {
            $('#output').empty();

            // 문자열인지 객체인지 확인
            if (typeof response === 'string') {
                $('#output').append(`<div class="no-project">${response}</div>`);
            } else {
                $(response).each(function(i, obj) {
                    let status = obj.projectStatus ? "모집완료" : "모집중";
                    let remainingDays = calculateRemainingDays(obj.projectStartDate, obj.projectEndDate);

                    let html = `
                        <div class="post">
                            <div class="post-info">
                                <a href="/myProject/management?projectNum=${obj.projectNum}">${obj.projectTitle}</a>
                                <span>금액: ${obj.projectBudget}원</span>
                                <span>기간: ${remainingDays}일</span>
                                <span>작성자: ${obj.clientId}</span>
                                <span>등록일: ${obj.recruitDeadline}</span>
                            </div>
                            <div class="additional-info">
                                <span class="status-badge">${status}</span>
                                <span>지원자: ${obj.numApplicants}명</span>
                                <span>마감일: ${obj.projectEndDate}</span>
                            </div>
                        </div>
                    `;
                    $('#output').append(html);
                });
            }
        },
        error: function(e) {
            alert('조회 실패');
        }
    });
}

function calculateRemainingDays(startDate, endDate) {
    const start = new Date(startDate);
    const end = new Date(endDate);
    const differenceInTime = end - start;
    const differenceInDays = Math.ceil(differenceInTime / (1000 * 60 * 60 * 24));
    return differenceInDays >= 0 ? differenceInDays : 0;
}