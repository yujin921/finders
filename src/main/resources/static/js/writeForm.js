let selectedScopes = [];
// 업무 범위 선택 기능 (다중 선택 가능)
function toggleSelect(element) {
    element.classList.toggle('selected');
    const scope = element.querySelector('input[type="checkbox"]').value;

    // 선택된 상태라면 배열에 추가
    if (element.classList.contains('selected')) {
        selectedScopes.push(scope);
    } else {
        // 선택 해제되면 배열에서 제거
        selectedScopes = selectedScopes.filter(s => s !== scope);
    }

    // 숨겨진 input 필드에 선택된 값을 저장 (폼 제출용)
    document.getElementById('selectedWorkScopes').value = selectedScopes.join(',');

    // 모집 인원의 select 필드 업데이트
    updateRecruitSelectOptions();
}

// 모집 인원 select 필드 업데이트 함수
function updateRecruitSelectOptions() {
    const recruitSelects = document.querySelectorAll('.recruit-team-section select[name="role"], .recruit-added-item select[name="role"]');

    recruitSelects.forEach(select => {
        // select 필드의 모든 옵션 제거
        select.innerHTML = '';

        // 선택된 업무 범위를 기준으로 옵션 추가
        selectedScopes.forEach(scope => {
            const option = document.createElement('option');
            option.value = scope;
            option.textContent = scope;
            select.appendChild(option);
        });
    });
}

let selectedCategories = [];
// 카테고리 선택 기능 (다중 선택 가능)
function toggleCategory(element) {
    element.classList.toggle('selected');
    const category = element.dataset.category;

    // 선택된 상태라면 배열에 추가
    if (element.classList.contains('selected')) {
        selectedCategories.push(category);
    } else {
        // 선택 해제되면 배열에서 제거
        selectedCategories = selectedCategories.filter(c => c !== category);
    }

    // 숨겨진 input 필드에 선택된 카테고리 저장
    document.getElementById('selectedCategories').value = selectedCategories.join(',');

    // 모집 인원의 카테고리 select 필드 업데이트
    updateRecruitCategoryOptions();
}

// 모집 인원 category select 필드 업데이트 함수
function updateRecruitCategoryOptions() {
    const recruitCategorySelects = document.querySelectorAll('.recruit-team-section select[name="category"], .recruit-added-item select[name="category"]');

    recruitCategorySelects.forEach(select => {
        // select 필드의 모든 옵션 제거
        select.innerHTML = '';

        // 선택된 카테고리를 기준으로 옵션 추가
        selectedCategories.forEach(category => {
            const option = document.createElement('option');
            option.value = category;
            option.textContent = category;
            select.appendChild(option);
        });
    });
}

// 선택한 기술들을 저장할 배열
let selectedTechs = [];

// 기술 버튼 클릭 시 호출되는 함수
function toggleTech(element) {
    const tech = element.getAttribute('data-tech');  // data-tech 속성에서 기술명 가져오기
    const selectedTechsContainer = document.getElementById('selected-techs');

    // 선택된 상태라면 선택 해제
    if (element.classList.contains('selected')) {
        element.classList.remove('selected');
        selectedTechs = selectedTechs.filter(t => t !== tech);  // 배열에서 제거
    } else {
        // 선택되지 않은 상태라면 선택 추가
        element.classList.add('selected');
        selectedTechs.push(tech);  // 배열에 추가
    }
    // 선택된 기술들을 상단에 표시
    selectedTechsContainer.innerHTML = selectedTechs
        .map(item => `<span class="selected-tech">${item}</span>`)
        .join('');
}

const checkFormInterval = setInterval(function() {
    const form = document.querySelector('form');
    if (form) {
        // 폼에 이벤트 리스너 추가
        form.addEventListener('submit', function(event) {
            const selectedSkillsInput = document.getElementById('selectedSkills');
            selectedSkillsInput.value = selectedTechs.join(',');
        });
        // 폼이 발견되면 더 이상 확인하지 않음
        clearInterval(checkFormInterval);
    }
}, 500);  // 0.5초마다 확인

// 상세 업무 내용 글자 수 카운팅
function countCharacters() {
    const detailTextarea = document.getElementById('detail-text');
    const charCount = document.getElementById('char-count');

    console.log(detailTextarea, charCount);

    detailTextarea.addEventListener('input', function () {
        const length = detailTextarea.value.length;
        charCount.textContent = `${length} / 5000`;
    });
}

// 모집 인원 추가 기능
function addTeamMember() {
    const teamMembersContainer = document.getElementById('recruit-team-members');
    const recruitDeadline = document.getElementById('recruit_deadline').value;
    // 새로운 팀원 입력 필드 생성
    const newTeamMember = document.createElement('div');
    newTeamMember.classList.add('recruit-added-item');
    newTeamMember.innerHTML = `
                <select name="role"></select>
                <select name="category"></select>
                <input type="number" min="1" placeholder="명" name="teamSize[]">
                <button type="button" onclick="remove(this)">삭제</button>
            `;
    teamMembersContainer.appendChild(newTeamMember);

    // 새로 추가된 팀원의 role select 필드 업데이트
    updateRecruitSelectOptions();
    updateRecruitCategoryOptions();

    document.getElementById('recruit_deadline').value = recruitDeadline;
}

// 사전 검증 질문 추가 기능
function addQuestion() {
    const questionsContainer = document.getElementById('recruit-questions');

    // 새로운 질문 입력 필드 생성
    const newQuestion = document.createElement('div');
    newQuestion.classList.add('recruit-added-question');
    newQuestion.innerHTML = `
                <input type="text" class="recruit-question-input" placeholder="사전 질문을 입력해주세요." name="question[]">
                <button type="button" onclick="remove(this)">삭제</button>
            `;
    questionsContainer.appendChild(newQuestion);
}

// Call the character counting function on page load
window.onload = function () {
    countCharacters();
};

function remove(button) {
    const teamMember = button.parentNode;
    teamMember.remove();
}