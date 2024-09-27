document.addEventListener('DOMContentLoaded', function () {
    const reviewDataElement = document.getElementById('reviewData');
    const projectNum = reviewDataElement.getAttribute('data-project-num');
    const userId = reviewDataElement.getAttribute('data-user-id');
    const userRole = reviewDataElement.getAttribute('data-user-role');

    console.log("ProjectNum: " + projectNum);
    console.log("UserId: " + userId);
    console.log("UserRole: " + userRole);

    if (!projectNum || !userId || !userRole) {
        console.error("ProjectNum, UserId 또는 UserRole이 설정되지 않았습니다.");
        return;
    }

    const participantSelect = document.getElementById('participantSelect');

    // 클라이언트일 경우 바로 프리랜서 목록을 로드
    if (userRole === 'ROLE_CLIENT') {
        fetchParticipants('freelancer');
    }

    // 프리랜서로 로그인한 경우에만 역할 선택 가능
    if (userRole !== 'ROLE_CLIENT') {
        const roleSelectElement = document.getElementById('roleSelect');

        if (roleSelectElement) {
            roleSelectElement.addEventListener('change', function () {
                const selectedRole = this.value;

                if (selectedRole) {
                    fetchParticipants(selectedRole);
                } else {
                    hideFormSections();
                }
            });
        } else {
            console.error("roleSelectElement를 찾을 수 없습니다.");
        }
    }

	function fetchParticipants(role) {
	    document.getElementById('participantSelection').style.display = 'block';

	    fetch(`/unifiedreview/getParticipants?projectNum=${projectNum}&userId=${userId}&role=${role}`)
	        .then(response => response.json())
	        .then(participants => {
	            console.log("Participants fetched:", participants);  // 서버 응답 확인

	            if (participants.length === 0) {
	                participantSelect.innerHTML = '<option value="">참가자가 없습니다.</option>';
	            } else {
	                participantSelect.innerHTML = participants.map(participant => {
	                    const disabledAttr = participant.reviewCompleted ? 'disabled' : '';
	                    const completedText = participant.reviewCompleted ? ' (작성 완료)' : '';
	                    return `<option value="${participant.id}" ${disabledAttr}>${participant.name}${completedText}</option>`;
	                }).join('');
	            }

	            document.getElementById('ratingSection').style.display = 'block';
	            document.getElementById('commentBox').style.display = 'block';
	            document.getElementById('buttonSection').style.display = 'block';
	            updateCheckboxGroup(role);
	        })
	        .catch(error => {
	            console.error("Error fetching participants: ", error);
	            participantSelect.innerHTML = '<option value="">참가자를 불러오는 데 오류가 발생했습니다.</option>';
	        });
	}

    function hideFormSections() {
        document.getElementById('participantSelection').style.display = 'none';
        document.getElementById('ratingSection').style.display = 'none';
        document.getElementById('checkboxGroup').style.display = 'none';
        document.getElementById('commentBox').style.display = 'none';
        document.getElementById('buttonSection').style.display = 'none';
    }

	function updateCheckboxGroup(role) {
	    const checkboxGroup = document.getElementById('checkboxGroup');
	    checkboxGroup.style.display = 'block';

	    let checkboxesHTML = '';
	    if (role === 'client') {
	        checkboxesHTML = `
	            <input type="checkbox" id="checkbox1" value="친절하고 협조적" />
	            <label for="checkbox1">친절하고 협조적</label><br />
	            <input type="checkbox" id="checkbox2" value="작업 퀄리티가 높아요" />
	            <label for="checkbox2">작업 퀄리티가 높아요</label><br />
	            <input type="checkbox" id="checkbox3" value="기술적 문제 해결" />
	            <label for="checkbox3">기술적 문제 해결</label><br />
	            <input type="checkbox" id="checkbox4" value="소통이 원활해요" />
	            <label for="checkbox4">소통이 원활해요</label><br />
	        `;
	    } else if (role === 'freelancer') {
	        checkboxesHTML = `
	            <input type="checkbox" id="checkbox1" value="전문성이 뛰어나요" />
	            <label for="checkbox1">전문성이 뛰어나요</label><br />
	            <input type="checkbox" id="checkbox2" value="기한을 잘 지켜요" />
	            <label for="checkbox2">기한을 잘 지켜요</label><br />
	            <input type="checkbox" id="checkbox3" value="문제 해결 능력이 뛰어나요" />
	            <label for="checkbox3">문제 해결 능력이 뛰어나요</label><br />
	            <input type="checkbox" id="checkbox4" value="커뮤니케이션이 원활해요" />
	            <label for="checkbox4">커뮤니케이션이 원활해요</label><br />
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
	      console.log("Submit 버튼 클릭됨");

	      const participantSelectElement = document.getElementById('participantSelect');
	      const roleSelectElement = document.getElementById('roleSelect');

		  
		  
	      // 클라이언트일 경우 역할 선택 부분이 없을 수 있으므로 이 조건을 추가
	      const selectedRole = roleSelectElement ? roleSelectElement.value : 'freelancer'; // 기본적으로 'freelancer'

	      if (!participantSelectElement || !participantSelectElement.value) {
	          alert('참가자가 선택되지 않았습니다.');
	          console.error("participantSelectElement가 비어있습니다.");
	          return;
	      }

	      const participantId = participantSelectElement.value;
	      const ratingText = document.getElementById('rating-text').textContent;
	      const rating = parseFloat(ratingText.replace('점', ''));
	      const comment = document.querySelector('.comment-box textarea').value;
	      const projectNum = document.getElementById('reviewData').getAttribute('data-project-num');
		  // 체크박스 선택 항목을 가져오는 부분
		  const selectedItems = Array.from(document.querySelectorAll('.checkbox-group input[type="checkbox"]:checked'))
		      .map(checkbox => ({ itemName: checkbox.value, selected: true }));

		  console.log("선택된 리뷰 아이템:", selectedItems);  // 여기에서 선택된 리뷰 아이템이 제대로 나오는지 확인


			  
	      if (!participantId || isNaN(rating) || !comment.trim()) {
	          alert('모든 항목을 입력하세요.');
	          return;
	      }

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
	              reviewItems: selectedItems,
	              projectNum
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