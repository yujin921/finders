document.addEventListener('DOMContentLoaded', function() {
    // 모달 요소들 가져오기
    const modal = document.getElementById("modal");
    const modalTitle = document.getElementById("modalTitle");
    const modalBudget = document.getElementById("modalBudget");
    const modalDuration = document.getElementById("modalDuration");
    const modalDescription = document.getElementById("modalDescription");
    const modalSolution = document.getElementById("modalSolution");
    const closeBtn = document.querySelector(".close");
    const modalRegistration = document.getElementById("modalRegistration");
    const modalRecruitment = document.getElementById("modalRecruitment");
    const modalContract = document.getElementById("modalContract");
    const modalCompletion = document.getElementById("modalCompletion");

    // 프로젝트 데이터
    const projectsData = [
        {
            title: "퀄리티가 중요한 플랫폼, 검증된 개발사와 구축한 O기업",
            budget: "6,000만 원",
            duration: "150일",
            description: '"신사업으로 국내 여행 플랫폼을 개발하고자 했습니다. 내부 리소스가 부족해 외주를 선택했지만, 만족스러운 퀄리티가 나오지 않았습니다."',
            solution: [
                "여행 플랫폼 개발 경험 있는 개발사와 매칭",
                "숙소, 액티비티 예약/결제 기능한 플랫폼 구축",
                "높은 퀄리티, 빠른 시장 진입으로 초기 경쟁력 확보"
            ],
            registration: "프로젝트 등록",
            recruitment: "3일",
            contract: "7일",
            completion: "150일"
        },
        // 다른 프로젝트 데이터도 이와 같은 형식으로 추가
		{
		    title: "퀄리티가 중요한 플랫폼, 검증된 개발사와 구축한 O기업",
		    budget: "6,000만 원",
		    duration: "150일",
		    description: '"신사업으로 국내 여행 플랫폼을 개발하고자 했습니다. 내부 리소스가 부족해 외주를 선택했지만, 만족스러운 퀄리티가 나오지 않았습니다."',
		    solution: [
		        "여행 플랫폼 개발 경험 있는 개발사와 매칭",
		        "숙소, 액티비티 예약/결제 기능한 플랫폼 구축",
		        "높은 퀄리티, 빠른 시장 진입으로 초기 경쟁력 확보"
		    ],
		    registration: "프로젝트 등록",
		    recruitment: "3일",
		    contract: "7일",
		    completion: "150일"
		},
		{
		    title: "퀄리티가 중요한 플랫폼, 검증된 개발사와 구축한 O기업",
		    budget: "6,000만 원",
		    duration: "150일",
		    description: '"신사업으로 국내 여행 플랫폼을 개발하고자 했습니다. 내부 리소스가 부족해 외주를 선택했지만, 만족스러운 퀄리티가 나오지 않았습니다."',
		    solution: [
		        "여행 플랫폼 개발 경험 있는 개발사와 매칭",
		        "숙소, 액티비티 예약/결제 기능한 플랫폼 구축",
		        "높은 퀄리티, 빠른 시장 진입으로 초기 경쟁력 확보"
		    ],
		    registration: "프로젝트 등록",
		    recruitment: "3일",
		    contract: "7일",
		    completion: "150일"
		},
		{
		    title: "퀄리티가 중요한 플랫폼, 검증된 개발사와 구축한 O기업",
		    budget: "6,000만 원",
		    duration: "150일",
		    description: '"신사업으로 국내 여행 플랫폼을 개발하고자 했습니다. 내부 리소스가 부족해 외주를 선택했지만, 만족스러운 퀄리티가 나오지 않았습니다."',
		    solution: [
		        "여행 플랫폼 개발 경험 있는 개발사와 매칭",
		        "숙소, 액티비티 예약/결제 기능한 플랫폼 구축",
		        "높은 퀄리티, 빠른 시장 진입으로 초기 경쟁력 확보"
		    ],
		    registration: "프로젝트 등록",
		    recruitment: "3일",
		    contract: "7일",
		    completion: "150일"
		}
    ];

    // 각 case-card에 클릭 이벤트 리스너 추가
    document.querySelectorAll('.case-card').forEach((card, index) => {
        card.addEventListener('click', () => openModal(index));
    });

    // 모달 열기 함수
    function openModal(index) {
        const project = projectsData[index];
        modalTitle.textContent = project.title;
        modalBudget.textContent = project.budget;
        modalDuration.textContent = project.duration;
        modalDescription.textContent = project.description;
        modalSolution.innerHTML = project.solution.map(item => `<li>${item}</li>`).join('');
        modalRegistration.textContent = project.registration;
        modalRecruitment.textContent = project.recruitment;
        modalContract.textContent = project.contract;
        modalCompletion.textContent = project.completion;

        modal.style.display = "block";
    }

    // 모달 닫기 함수
    function closeModal() {
        modal.style.display = "none";
    }

    // 닫기 버튼 클릭 시 모달 닫기
    closeBtn.onclick = closeModal;

    // 모달 외부 클릭 시 모달 닫기
    window.onclick = function(event) {
        if (event.target == modal) {
            closeModal();
        }
    }

    // 스타트업 사례집 다운로드 버튼 클릭 이벤트
    document.getElementById("downloadCase").addEventListener('click', function() {
        alert("스타트업 사례집 다운로드를 시작합니다.");
        // 여기에 실제 다운로드 로직을 구현할 수 있습니다.
    });

    // 프로젝트 등록하기 버튼 클릭 이벤트
    document.getElementById("registerProject").addEventListener('click', function() {
        window.location.href = "/project/register"; // 프로젝트 등록 페이지로 이동
    });
});