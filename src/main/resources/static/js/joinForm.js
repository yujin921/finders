// joinForm.js
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

    // 프로필 이미지 미리보기
    document.querySelectorAll('input[type="file"]').forEach(input => {
        input.addEventListener('change', function(event) {
            const file = event.target.files[0];
            const preview = document.getElementById(`${event.target.id}-preview`);

            if (file) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    preview.src = e.target.result;
                }
                reader.readAsDataURL(file);
            } else {
                preview.src = 'default-profile.png';
            }
        });
    });
});
