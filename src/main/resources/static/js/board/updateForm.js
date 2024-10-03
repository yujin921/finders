let selectedScopes = [...initialSelectedScopes]; // 초기 선택된 범위 담기

// 업무 범위 선택 기능 (다중 선택 가능)
function toggleSelect(element) {
    const scope = element.querySelector('input[type="checkbox"]').value;

    if (element.classList.contains('selected')) {
        // 선택 해제 시
        element.classList.remove('selected');
        selectedScopes = selectedScopes.filter(s => s !== scope);

        // select 필드에서 제거
        removeOptionFromSelect('role', scope);
    } else {
        // 선택 시
        element.classList.add('selected');
        if (!selectedScopes.includes(scope)) {
            selectedScopes.push(scope);
        }
    }

    // 선택된 범위들을 숨겨진 input 필드에 저장 (폼 제출용)
    document.getElementById('selectedWorkScopes').value = selectedScopes.join(',');

    // select 필드를 업데이트
    updateRecruitSelectOptions();
}

// select 목록에서 옵션 제거 함수
function removeOptionFromSelect(selectName, valueToRemove) {
    const recruitSelects = document.querySelectorAll(`select[name="${selectName}"]`);

    recruitSelects.forEach(select => {
        const optionToRemove = [...select.options].find(option => option.value === valueToRemove);
        if (optionToRemove) {
            select.removeChild(optionToRemove);
        }
    });
}

// 모집 인원 select 필드 업데이트 함수
function updateRecruitSelectOptions() {
    const recruitSelects = document.querySelectorAll('select[name="role"]');  // 모든 role select 필드 찾기

    recruitSelects.forEach(select => {
        const selectedValue = select.value;

        // 기존 옵션 유지 + 새로운 범위 추가
        selectedScopes.forEach(scope => {
            if (![...select.options].some(option => option.value === scope)) {
                const option = document.createElement('option');
                option.value = scope;
                option.textContent = scope;
                select.appendChild(option);
            }
        });

        // 선택된 값을 유지
        if (selectedValue && selectedScopes.includes(selectedValue)) {
            select.value = selectedValue;
        }
    });
}


let selectedCategories = [...initialSelectedCategories]; // 초기 선택된 카테고리 담기

// 카테고리 선택 기능 (다중 선택 가능)
function toggleCategory(element) {
    const category = element.dataset.category;

    if (element.classList.contains('selected')) {
        // 선택 해제 시
        element.classList.remove('selected');
        selectedCategories = selectedCategories.filter(c => c !== category);

        // select 필드에서 제거
        removeOptionFromSelect('category', category);
    } else {
        // 선택 시
        element.classList.add('selected');
        if (!selectedCategories.includes(category)) {
            selectedCategories.push(category);
        }
    }

    document.getElementById('selectedCategories').value = selectedCategories.join(',');
    updateRecruitCategoryOptions();
}

// 모집 인원 category select 필드 업데이트 함수
function updateRecruitCategoryOptions() {
    const recruitCategorySelects = document.querySelectorAll('select[name="category"]');

    recruitCategorySelects.forEach(select => {
        const selectedValue = select.value;

        // 기존 옵션 유지 + 새로운 카테고리 추가
        selectedCategories.forEach(category => {
            if (![...select.options].some(option => option.value === category)) {
                const option = document.createElement('option');
                option.value = category;
                option.textContent = category;
                select.appendChild(option);
            }
        });

        // 선택된 값을 유지
        if (selectedValue && selectedCategories.includes(selectedValue)) {
            select.value = selectedValue;
        }
    });
}


let selectedTechs = [...initialSelectedTechs];  // 초기 선택된 기술 담기

// 기술 버튼 클릭 시 호출되는 함수
function toggleTech(element) {
    const tech = element.getAttribute('data-tech');
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

    // 선택된 기술 상단에 표시
    selectedTechsContainer.innerHTML = selectedTechs.map(item => `<span class="selected-tech">${item}</span>`).join('');
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
                <button type="button" class="delete-button" onclick="remove(this)">삭제</button>
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
                <button type="button" class = "delete-button" onclick="remove(this)">삭제</button>
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

document.addEventListener('DOMContentLoaded', function () {
    // 초기 선택된 기술 처리
    const selectedTechsContainer = document.getElementById('selected-techs');
    selectedTechs = [...initialSelectedTechs];

    // 선택된 기술 상단에 표시
    selectedTechsContainer.innerHTML = selectedTechs
        .map(item => `<span class="selected-tech">${item}</span>`)
        .join('');
    console.log("Selected Techs Container:", selectedTechsContainer.innerHTML); // 선택된 기술 표시 확인

    // 페이지 로드 시 기존 선택된 기술들에 대해 버튼도 활성화
    initialSelectedTechs.forEach(tech => {
        const techButton = document.querySelector(`[data-tech="${tech}"]`);
        if (techButton) {
            techButton.classList.add('selected');
        }
    });

    // 범위 선택 값으로 모집 인원 select 업데이트
    updateRecruitSelectOptions(initialSelectedScopes);

    // 카테고리 선택 값으로 모집 인원 select 업데이트
    updateRecruitCategoryOptions(initialSelectedCategories);

    // 섹션 및 사이드바 메뉴 처리
    const sections = document.querySelectorAll(".section");
    const menuItems = document.querySelectorAll("#step-list li");

    window.addEventListener("scroll", function () {
        const scrollPosition = window.pageYOffset || document.documentElement.scrollTop;

        sections.forEach((section, index) => {
            const sectionTop = section.offsetTop - 100;
            const sectionHeight = section.offsetHeight;

            if (scrollPosition >= sectionTop && scrollPosition < sectionTop + sectionHeight) {
                menuItems.forEach(item => item.classList.remove("active"));
                menuItems[index].classList.add("active");
            }
        });
    });

    // 사이드바 항목 클릭 이벤트
    menuItems.forEach(item => {
        item.addEventListener("click", function (e) {
            e.preventDefault();
            const targetId = this.getAttribute("data-target");
            const targetSection = document.getElementById(targetId);

            if (targetSection) {
                window.scrollTo({
                    top: targetSection.offsetTop - 100,
                    behavior: 'smooth'
                });
            }
        });
    });
});