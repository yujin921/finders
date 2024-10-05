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
//    searchButton.addEventListener('click', () => {
//        const query = searchInput.value.trim();
//        if (query) {
//            console.log(`검색어: ${query}`);
//            // 검색 로직을 여기에 추가합니다. 예를 들어, 검색 페이지로 이동할 수 있습니다.
//            // window.location.href = `/search?query=${encodeURIComponent(query)}`;
//        }
//    });

    // 시작 시 자동 슬라이드 변경
    startAutoSlide();
});

$(document).ready(function(){
    const reviewSlider = $('.review-slider');

    // 슬라이더 요소가 있는지 확인
    if (reviewSlider.length > 0) {
        // AJAX로 서버에서 리뷰 데이터를 가져옴
        $.ajax({
            url: '/unifiedreview/latest',
            method: 'GET',
            success: function(response) {
                response.forEach(function(review) {
                    let reviewHtml = `
                        <div class="review-item">
                            <p class="review-text">${review.comment || '리뷰 내용이 없습니다.'}</p>
                            <div class="rating-and-author">
                                <div class="rating">${'★'.repeat(Math.floor(review.rating))}</div>
                                <div class="author-id">${review.reviewerId || '익명'}</div>
                            </div>
                            <div class="review-footer">
                                <div class="recipient-info">
                                    <img src="${review.profileImg || '/images/default-profile.png'}" alt="${review.receivedId || '수신자 미상'}의 프로필 이미지">
                                    <span class="recipient-role">${review.role || '역할 미상'}</span>
                                    <span class="recipient-id">${review.receivedId || '수신자 미상'}</span>
                                </div>
                                <div class="review-date">${review.reviewDate.split('T')[0] || '날짜 미상'}</div>
                            </div>
                        </div>`;
                    
                    reviewSlider.append(reviewHtml);
                });

                // 슬라이더 초기화
                if (!reviewSlider.hasClass('slick-initialized')) {
					reviewSlider.slick({
					    infinite: true,   // 무한 슬라이드 활성화
					    slidesToShow: 3,  // 한 번에 표시할 슬라이드 수
					    slidesToScroll: 1,
					    autoplay: true,   // 자동 슬라이드 활성화
					    autoplaySpeed: 0, // 자동 슬라이드 딜레이 없이 부드럽게 이동
					    speed: 6000,      // 슬라이드가 전환되는 시간 (6초 동안 전환)
					    cssEase: 'linear', // 부드러운 전환을 위해 'linear' easing 사용
					    dots: true,       // 하단 네비게이션 점 표시
					    variableWidth: true,  // 슬라이드 너비가 유동적으로 조정
					    centerMode: true  // 슬라이드가 화면 가운데로
					});
                }
            },
            error: function() {
                console.error('리뷰 데이터를 불러오는 데 실패했습니다.');
            }
        });
    } else {
        console.error('리뷰 슬라이더 요소를 찾을 수 없습니다.');
    }
});





