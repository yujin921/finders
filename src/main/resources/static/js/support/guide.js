document.addEventListener('DOMContentLoaded', function() {
  const tabs = document.querySelectorAll('.main-tabs button');
  const tabContents = document.querySelectorAll('.tab-content');
  
  tabs.forEach(tab => {
    tab.addEventListener('click', function() {
      tabs.forEach(t => t.classList.remove('active'));
      this.classList.add('active');
      
      const target = this.getAttribute('data-tab');
      tabContents.forEach(content => {
        content.style.display = content.id === target ? 'block' : 'none';
      });
    });
  });

  // 초기 상태 설정
  tabs[0].click();
});