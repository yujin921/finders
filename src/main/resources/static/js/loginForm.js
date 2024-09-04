document.addEventListener('DOMContentLoaded', function() {
    const tabs = document.querySelectorAll('.tabs button');
    const tabContents = document.querySelectorAll('.tab-content');
    
    tabs.forEach(tab => {
        tab.addEventListener('click', function() {
            // 탭 버튼 활성화 상태 변경
            tabs.forEach(t => t.classList.remove('active'));
            this.classList.add('active');
            
            // 탭 콘텐츠 표시 상태 변경
            const target = this.getAttribute('data-tab');
            tabContents.forEach(content => {
                if (content.id === target) {
                    content.classList.add('active');
                } else {
                    content.classList.remove('active');
                }
            });
        });
    });
    
    // 페이지가 완전히 로드된 후 에러 메시지를 처리합니다.
    const errorMessage = document.querySelector('.errorMsg');

    if (errorMessage) {
        alert(errorMessage.innerText.trim());
    }
});