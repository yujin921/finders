document.addEventListener('DOMContentLoaded', function () {
    // reviewData 요소에서 데이터 가져오기
    const reviewDataElement = document.getElementById('reviewData');
    const projectNum = reviewDataElement.getAttribute('data-project-num');
    const userId = reviewDataElement.getAttribute('data-user-id');

    console.log("ProjectNum: " + projectNum);
    console.log("UserId: " + userId);

    if (!projectNum || !userId) {
        console.error("ProjectNum 또는 UserId가 설정되지 않았습니다.");
        return;
    }

    // 역할 선택에 따라 평가 폼이 변경되도록 처리
    const roleSelectElement = document.getElementById('roleSelect');
    
    if (roleSelectElement) {
        roleSelectElement.addEventListener('change', function () {
            const selectedRole = this.value;
            const participantSelect = document.getElementById('participantSelect');

            if (selectedRole) {
                // 평가 대상 선택 박스를 보여줌
                document.getElementById('participantSelection').style.display = 'block';

                // 역할에 따라 서버에서 적절한 참가자 목록 가져오기
                fetch(`/unifiedreview/getParticipants?projectNum=${projectNum}&userId=${userId}&role=${selectedRole}`)
                    .then(response => response.json())
                    .then(participants => {
                        // 참여자 목록을 역할에 맞게 필터링 후 채우기
                        participantSelect.innerHTML = participants.map(participant =>
                            `<option value="${participant.id}">${participant.name}</option>`
                        ).join('');

                        // 나머지 폼 보이기
                        document.getElementById('ratingSection').style.display = 'block';
                        document.getElementById('commentBox').style.display = 'block';
                        document.getElementById('buttonSection').style.display = 'block';

                        // 역할에 따라 체크박스 평가 항목 변경
                        updateCheckboxGroup(selectedRole);
                    });
            } else {
                // 선택이 없을 때 폼을 숨김
                document.getElementById('participantSelection').style.display = 'none';
                document.getElementById('ratingSection').style.display = 'none';
                document.getElementById('checkboxGroup').style.display = 'none';
                document.getElementById('commentBox').style.display = 'none';
                document.getElementById('buttonSection').style.display = 'none';
            }
        });
    } else {
        console.error("roleSelectElement를 찾을 수 없습니다.");
    }

    // 역할에 따라 체크박스 그룹 업데이트
    function updateCheckboxGroup(role) {
        const checkboxGroup = document.getElementById('checkboxGroup');
        checkboxGroup.style.display = 'block';

        let checkboxesHTML = '';
        if (role === 'client') {
            // 클라이언트에 대한 평가 항목
            checkboxesHTML = `
                <input type="checkbox" value="친절하고 협조적" /> 친절하고 협조적<br />
                <input type="checkbox" value="작업 퀄리티가 높아요" /> 작업 퀄리티가 높아요<br />
                <input type="checkbox" value="기술적 문제 해결" /> 기술적 문제 해결<br />
                <input type="checkbox" value="소통이 원활해요" /> 소통이 원활해요<br />
            `;
        } else if (role === 'freelancer') {
            // 프리랜서에 대한 평가 항목
            checkboxesHTML = `
                <input type="checkbox" value="전문성이 뛰어나요" /> 전문성이 뛰어나요<br />
                <input type="checkbox" value="기한을 잘 지켜요" /> 기한을 잘 지켜요<br />
                <input type="checkbox" value="문제 해결 능력이 뛰어나요" /> 문제 해결 능력이 뛰어나요<br />
                <input type="checkbox" value="커뮤니케이션이 원활해요" /> 커뮤니케이션이 원활해요<br />
            `;
        }

        checkboxGroup.innerHTML = checkboxesHTML;
    }


    // 별점 선택 기능
    document.querySelectorAll('.star').forEach(star => {
        star.addEventListener('click', () => {
            let rating = parseFloat(star.getAttribute('data-value'));
            document.getElementById('rating-text').textContent = `${rating}점`;

            // 모든 별을 비활성화
            document.querySelectorAll('.star').forEach(s => s.classList.remove('selected'));

            // 선택한 별까지 활성화
            document.querySelectorAll('.star').forEach(s => {
                if (parseFloat(s.getAttribute('data-value')) <= rating) {
                    s.classList.add('selected');
                }
            });
        });
    });

    // 체크박스 선택 제한 (최대 5개까지)
    const checkboxes = document.querySelectorAll('.checkbox-group input[type="checkbox"]');
    checkboxes.forEach(checkbox => {
        checkbox.addEventListener('change', () => {
            if (document.querySelectorAll('.checkbox-group input[type="checkbox"]:checked').length > 5) {
                checkbox.checked = false;
                alert('최대 5개 항목만 선택할 수 있습니다.');
            }
        });
    });

    // 리뷰 제출 처리
    document.getElementById('submitButton').addEventListener('click', function () {
        const selectedRole = document.getElementById('roleSelect').value;
        const participantId = document.getElementById('participantSelect').value;
        const ratingText = document.getElementById('rating-text').textContent;
        const rating = parseFloat(ratingText.replace('점', ''));
        const comment = document.querySelector('.comment-box textarea').value;
        const selectedItems = Array.from(document.querySelectorAll('.checkbox-group input[type="checkbox"]:checked'))
            .map(checkbox => ({ itemName: checkbox.value, selected: true }));

        if (!participantId || isNaN(rating) || !comment.trim()) {
            alert('모든 항목을 입력하세요.');
            return;
        }

        // 등록 전 경고 문구
        const confirmRegistration = confirm("한 번 등록한 평가는 수정할 수 없습니다. 계속하시겠습니까?");
        if (!confirmRegistration) return;

        const endpoint = selectedRole === 'client' ? '/unifiedreview/submitClientReview' : '/unifiedreview/submitFreelancerReview';

        fetch(endpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                participantId,
                rating,
                comment,
                reviewItems: selectedItems
            })
        })
        .then(response => {
            if (response.ok) {
                alert('리뷰가 제출되었습니다.');
                window.location.href = '/project/list';
            } else {
                response.text().then(text => alert(`리뷰 제출에 실패했습니다: ${text}`));
            }
        })
        .catch(error => {
            console.error('리뷰 제출 오류:', error);
            alert('리뷰 제출 중 오류가 발생했습니다.');
        });
    });
});
