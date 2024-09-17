document.addEventListener('DOMContentLoaded', function() {
    // 피쳐 카드 애니메이션
    const cards = document.querySelectorAll('.feature-card');
    cards.forEach(card => {
        card.addEventListener('mouseover', () => {
            card.style.backgroundColor = '#f0f0f0';
        });
        card.addEventListener('mouseout', () => {
            card.style.backgroundColor = 'white';
        });
    });

    // 간단한 testimonial 슬라이더
    const testimonials = document.querySelectorAll('.testimonial');
    let currentTestimonial = 0;

    function showNextTestimonial() {
        testimonials[currentTestimonial].style.display = 'none';
        currentTestimonial = (currentTestimonial + 1) % testimonials.length;
        testimonials[currentTestimonial].style.display = 'block';
    }

    setInterval(showNextTestimonial, 5000); // 5초마다 testimonial 변경
});