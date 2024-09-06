document.addEventListener('DOMContentLoaded', () => {
    const slidesContainer = document.querySelector('.slides');
    const slides = Array.from(document.querySelectorAll('.slide'));
    const prevButton = document.querySelector('.prev');
    const nextButton = document.querySelector('.next');
    const searchButton = document.querySelector('#search-button');
    const searchInput = document.querySelector('#search-input');

    // 클론 생성
    slides.forEach(slide => {
        const clone = slide.cloneNode(true);
        slidesContainer.appendChild(clone);
    });

    const totalSlides = slides.length;
    const slideWidth = 100 / totalSlides; // 슬라이드 개수에 따라 슬라이드 너비 설정
    slidesContainer.style.width = `${totalSlides * 100}%`; // 슬라이드 컨테이너의 너비 설정

    let currentIndex = 0;
    let isTransitioning = false;
    let autoSlideInterval;

    function showSlide(index) {
        if (isTransitioning) return; // 전환 중일 때는 다른 전환을 방지

        isTransitioning = true;
        
        if (index >= totalSlides) {
            slidesContainer.style.transition = 'none'; // 트랜지션 비활성화
            slidesContainer.style.transform = `translateX(0)`; // 마지막 이미지에서 첫 번째 이미지로 이동
            currentIndex = 0;
            setTimeout(() => {
                slidesContainer.style.transition = 'transform 1s ease-in-out'; // 트랜지션 재활성화
                slidesContainer.style.transform = `translateX(-${currentIndex * slideWidth}%)`; // 첫 번째 이미지로 이동
                setTimeout(() => {
                    isTransitioning = false; // 전환이 완료된 후, 다시 전환 가능
                }, 1000); // 슬라이드 전환 애니메이션 시간과 맞추기
            }, 50); // 브라우저가 스타일을 리프레시할 시간을 줍니다
        } else if (index < 0) {
            slidesContainer.style.transition = 'none';
            slidesContainer.style.transform = `translateX(-${(totalSlides - 1) * 100}%)`; // 첫 번째 이미지에서 마지막 이미지로 이동
            currentIndex = totalSlides - 1;
            setTimeout(() => {
                slidesContainer.style.transition = 'transform 1s ease-in-out';
                slidesContainer.style.transform = `translateX(-${currentIndex * slideWidth}%)`; // 마지막 이미지로 이동
                setTimeout(() => {
                    isTransitioning = false; // 전환이 완료된 후, 다시 전환 가능
                }, 1000); // 슬라이드 전환 애니메이션 시간과 맞추기
            }, 50);
        } else {
            currentIndex = index;
            slidesContainer.style.transform = `translateX(-${currentIndex * slideWidth}%)`;
            setTimeout(() => {
                isTransitioning = false; // 전환이 완료된 후, 다시 전환 가능
            }, 1000); // 슬라이드 전환 애니메이션 시간과 맞추기
        }
    }

    function startAutoSlide() {
        autoSlideInterval = setInterval(() => {
            if (!isTransitioning) {
                showSlide(currentIndex + 1);
            }
        }, 3000); // 3초마다 슬라이드 변경
    }

    function stopAutoSlide() {
        clearInterval(autoSlideInterval);
    }

    nextButton.addEventListener('click', () => {
        stopAutoSlide();
        showSlide(currentIndex + 1);
        startAutoSlide();
    });

    prevButton.addEventListener('click', () => {
        stopAutoSlide();
        showSlide(currentIndex - 1);
        startAutoSlide();
    });

    // 검색 버튼 클릭 시 동작
    searchButton.addEventListener('click', () => {
        const query = searchInput.value.trim();
        if (query) {
            console.log(`검색어: ${query}`);
            // 검색 로직을 여기에 추가합니다. 예를 들어, 검색 페이지로 이동할 수 있습니다.
            // window.location.href = `/search?query=${encodeURIComponent(query)}`;
        }
    });

    // 시작 시 자동 슬라이드 변경
    startAutoSlide();
});
