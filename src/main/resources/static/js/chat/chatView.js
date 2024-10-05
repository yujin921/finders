/**
 * 
 */


//전역 변수 선언
let selectedMembers = [];
let selectedProjectNum = null;
let stompClients = {};

// 사이드바 열기
function openSidebar() {
    document.getElementById("chatSidebar").style.width = "300px";
    updateChatRoomList();
}

// 사이드바 닫기
function closeSidebar() {
    document.getElementById("chatSidebar").style.width = "0";
}

//채팅방 목록 업데이트 함수
function updateChatRoomList() {
    fetch('/chat/getChatRooms')
        .then(response => response.json())
        .then(chatrooms => {
            const chatRoomsContent = document.getElementById('chatRoomsContent');
            chatRoomsContent.innerHTML = '';

            chatrooms.forEach(room => {
                // 참가자 수를 확인하고 필요시 메시지를 삭제하는 부분 추가
                

                // 채팅방을 UI에 추가
                const roomContainer = document.createElement('div');
                roomContainer.classList.add('chat-room-container');

                const roomLink = document.createElement('a');
                roomLink.classList.add('chat-room-item');
                roomLink.textContent = room.chatroomName;
                roomLink.href = '#';
                roomLink.setAttribute('data-chatroom-id', room.chatroomId);
                roomLink.onclick = (event) => {
                    event.preventDefault();
                    openChatRoom(room.chatroomId, room.chatroomName);
                };

                roomContainer.appendChild(roomLink);
                chatRoomsContent.appendChild(roomContainer);
            });
        })
        .catch(error => {
            console.error('Error fetching chat rooms:', error);
            alert('채팅방 목록을 불러오는 중 오류가 발생했습니다.');
        });
}

//채팅방 삭제 API 호출 예시
function deleteEmptyChatRooms(chatRoomId) {
    fetch(`/chat/deleteIfNoParticipants?chatRoomId=${chatRoomId}`, {
        method: 'DELETE'
    })
    .then(response => {
        if (response.ok) {
            console.log("참가자가 없는 채팅방을 삭제했습니다.");
        } else {
            console.error("채팅방 삭제 실패");
        }
    })
    .catch(error => {
        console.error('삭제 요청 중 오류 발생:', error);
    });
}

let selectedChatRoomId = null; // 선택된 채팅방 ID 저장 변수


// DOMContentLoaded 시 이벤트 리스너 추가
document.addEventListener('DOMContentLoaded', function () {
    const chatRoomsContent = document.getElementById('chatRoomsContent');
    const leaveChatRoomButton = document.getElementById('leaveChatRoomButton');

    // 우클릭 이벤트 추가
    chatRoomsContent.addEventListener('contextmenu', function (event) {
        event.preventDefault();

        const target = event.target.closest('.chat-room-item');
        if (!target) return;

        selectedChatRoomId = target.getAttribute('data-chatroom-id');

        const contextMenu = document.getElementById('contextMenu');
        contextMenu.style.top = `${event.clientY}px`;
        contextMenu.style.left = `${event.clientX}px`;
        contextMenu.style.display = 'block';
    });

    // 클릭 시 컨텍스트 메뉴 닫기
    document.addEventListener('click', function () {
        document.getElementById('contextMenu').style.display = 'none';
    });

    // "채팅방 나가기" 버튼에 클릭 이벤트 추가
    leaveChatRoomButton.addEventListener('click', leaveChatRoom);
});

//채팅방 나가기 기능
function leaveChatRoom() {
    if (!selectedChatRoomId) {
        alert('채팅방을 선택하지 않았습니다.');
        return;
    }

    console.log(`Leaving chat room with ID: ${selectedChatRoomId}`);

    fetch(`/chat/leave?chatroomId=${selectedChatRoomId}`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'}
    })
        .then(response => {
            console.log(`Server response: ${response.status}`);
            if (response.ok) {
                alert('채팅방을 나갔습니다.');
                // 채팅방 목록을 갱신하여 삭제 여부를 확인
                updateChatRoomList();
            } else {
                alert('채팅방 나가기에 실패했습니다.');
            }
        })
        .catch(error => {
            console.error('Error leaving chat room:', error);
            alert('채팅방 나가기 중 오류가 발생했습니다.');
        });
}

function openChatRoom(chatroomId, chatroomName) {
    if (!chatroomId) {
        console.error('Invalid chatroomId:', chatroomId);
        return;
    }

    // chatroomId에 맞는 projectNum을 서버에서 가져오는 로직 추가
    fetch(`/chat/getProjectNum?chatroomId=${chatroomId}`)
        .then(response => response.json())
        .then(data => {
            const projectNum = data.projectNum; // 서버에서 받아온 projectNum 설정
            if (projectNum) {
                selectedProjectNum = projectNum; // 받아온 projectNum을 selectedProjectNum에 설정
                document.getElementById('chatroom-data').setAttribute('data-project-num', selectedProjectNum);
                console.log('Selected projectNum set in chatroom-data:', selectedProjectNum);
            } else {
                console.error('Could not fetch projectNum for chatroomId:', chatroomId);
            }
        })
        .catch(error => {
            console.error('Error fetching projectNum:', error);
        });

    // memberId를 서버에서 받아오는 로직 추가 (로그인한 사용자 정보)
    fetch('/member/getMemberId') // 서버에서 현재 로그인한 사용자의 memberId를 가져오는 API 엔드포인트
        .then(response => response.json())
        .then(data => {
            const memberId = data.memberId; // 서버에서 받아온 memberId 설정
            if (memberId) {
                document.getElementById('chatroom-data').setAttribute('data-member-id', memberId);
                console.log('Member ID set in chatroom-data:', memberId);
            } else {
                console.error('Could not fetch memberId');
            }
        })
        .catch(error => {
            console.error('Error fetching memberId:', error);
        });

    document.getElementById('chatOverlay').style.display = 'block';
    document.getElementById('chatModal').style.display = 'block';
    document.getElementById('chatroomTitle').textContent = chatroomName;
    document.getElementById('chatroom-data').setAttribute('data-chatroom-id', chatroomId);

    document.getElementById('messages').innerHTML = '';
    loadPreviousMessages(chatroomId);
    connectWebSocket(chatroomId);
}

// 채팅방 모달 닫기
function closeChatModal() {
    document.getElementById('chatOverlay').style.display = 'none';
    document.getElementById('chatModal').style.display = 'none';

    const chatroomElement = document.getElementById('chatroom-data');
    const chatroomId = chatroomElement.getAttribute('data-chatroom-id');

    if (stompClients[chatroomId]) {
        stompClients[chatroomId].disconnect(() => {
            console.log(`Disconnected from chatroom ${chatroomId}`);
        });
        delete stompClients[chatroomId];
    }
}

// WebSocket 연결 설정
function connectWebSocket(chatroomId) {
    if (!chatroomId) {
        console.error('chatroomId is undefined in connectWebSocket');
        return;
    }

    if (stompClients[chatroomId]) {
        console.log(`Already connected to chatroom ${chatroomId}`);
        return;
    }

    const socket = new SockJS('/ws/chat');
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe(`/topic/messages/${chatroomId}`, function (message) {
            const messageData = JSON.parse(message.body);
            displayMessage(messageData, chatroomId);
        });

        stompClients[chatroomId] = stompClient;
        console.log(`Connected to chatroom ${chatroomId}`);
    }, function (error) {
        console.error('WebSocket connection error:', error);
        alert('서버와의 연결에 문제가 발생했습니다. 다시 시도해주세요.');
    });
}

// 메시지 전송 함수
function sendMessage() {
    const messageInput = document.getElementById('messageInput');
    const messageContent = messageInput.value;
    const memberId = document.getElementById('chatroom-data').getAttribute('data-member-id');
    const chatroomId = document.getElementById('chatroom-data').getAttribute('data-chatroom-id');

    if (messageContent.trim() !== '' && stompClients[chatroomId]) {
        const messageData = {
            chatroomId: parseInt(chatroomId),
            senderId: memberId,
            messageContents: messageContent,
            sendTime: new Date().toISOString()
        };
        stompClients[chatroomId].send('/app/send', {}, JSON.stringify(messageData));
        messageInput.value = '';
    }
}

// 전송 버튼 클릭 이벤트
document.getElementById('sendMessageButton').onclick = sendMessage;

// Enter 키 입력 이벤트
document.getElementById('messageInput').addEventListener('keypress', function(event) {
    if (event.key === 'Enter') {
        event.preventDefault(); // 기본 동작 방지 (새 줄 추가 방지)
        sendMessage();
    }
});

//메시지 표시
function displayMessage(message, chatroomId) {
    const currentChatroomId = document.getElementById('chatroom-data').getAttribute('data-chatroom-id');
    if (message.chatroomId != currentChatroomId) {
        return;
    }

    const messageContainer = document.getElementById('messages');

    // 메시지 전체를 감싸는 외부 컨테이너 생성
    const messageWrapper = document.createElement('div');
    messageWrapper.classList.add('message-container');

    // 본인 메시지와 상대방 메시지 구분
    const memberId = document.getElementById('chatroom-data').getAttribute('data-member-id');
    if (message.senderId === memberId) {
        messageWrapper.classList.add('sent'); // 본인 메시지 외부 컨테이너 스타일
    } else {
        messageWrapper.classList.add('received'); // 상대방 메시지 외부 컨테이너 스타일
    }

    // 보낸 사람 ID를 담을 요소 생성
    const senderElement = document.createElement('div');
    senderElement.classList.add('sender');
    senderElement.textContent = message.senderId;

    // 실제 메시지 박스 생성
    const messageElement = document.createElement('div');
    messageElement.classList.add('message');
    messageElement.innerHTML = `<p class="text">${message.messageContents}</p>`;

    // 시간 표시 요소 생성
    const timestampElement = document.createElement('span');
    timestampElement.classList.add('timestamp');
    timestampElement.textContent = new Date(message.sendTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    // 메시지 요소들을 외부 컨테이너에 추가
    messageWrapper.appendChild(senderElement);
    messageWrapper.appendChild(messageElement);
    messageWrapper.appendChild(timestampElement);

    // 전체 메시지 컨테이너를 메시지 영역에 추가
    messageContainer.appendChild(messageWrapper);
    messageContainer.scrollTop = messageContainer.scrollHeight;
}


// 이전 메시지 로드
function loadPreviousMessages(chatroomId) {
    fetch(`/chat/messages?chatroomId=${chatroomId}`)
        .then(response => response.json())
        .then(messages => {
            messages.forEach(message => displayMessage(message, chatroomId));
        })
        .catch(error => {
            console.error('Failed to load messages:', error);
        });
}

// 참가자 모달 열기
function openParticipantsModal() {
    const chatroomId = document.getElementById('chatroom-data').getAttribute('data-chatroom-id');

    if (!chatroomId || isNaN(chatroomId)) {
        console.error('유효하지 않은 chatroomId:', chatroomId);
        alert('유효한 채팅방 ID가 필요합니다.');
        return;
    }

    fetch(`/chat/participants?chatroomId=${chatroomId}`)
        .then(response => response.json())
        .then(participants => {
            const participantList = document.getElementById('participantList');
            participantList.innerHTML = '';
            participants.forEach(participant => {
                const li = document.createElement('li');
                li.textContent = participant;
                participantList.appendChild(li);
            });
            document.getElementById('participantsModal').style.display = 'block';
            document.getElementById('overlay').style.display = 'block';
        })
        .catch(error => {
            console.error('Failed to load participants:', error);
            alert('참가자 목록을 불러오는 중 오류가 발생했습니다.');
        });
}

// 참가자 모달 닫기
function closeParticipantsModal() {
    document.getElementById('participantsModal').style.display = 'none';
    document.getElementById('overlay').style.display = 'none';
}

function openInviteModal() {
    const projectNum = document.getElementById('chatroom-data').getAttribute('data-project-num');
    const chatroomId = document.getElementById('chatroom-data').getAttribute('data-chatroom-id');

    // projectNum과 chatroomId가 올바르게 설정되었는지 확인
    if (!projectNum || isNaN(projectNum) || !chatroomId || isNaN(chatroomId)) {
        alert('유효한 프로젝트 번호 또는 채팅방 ID가 필요합니다.');
        return;
    }

    fetch(`/chat/getAvailableTeamMembers?projectNum=${projectNum}&chatroomId=${chatroomId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to load members'); // 서버에서 오류가 발생한 경우 예외 처리
            }
            return response.json();
        })
        .then(members => {
            const inviteList = document.getElementById('inviteList');
            if (!inviteList) {
                console.error('Invite list element not found'); // 요소가 없을 때의 처리
                return;
            }
            inviteList.innerHTML = '';
            if (!Array.isArray(members)) {
                alert('멤버 목록을 불러오는 중 오류가 발생했습니다.');
                return;
            }

            members.forEach(member => {
                const li = document.createElement('li');
                li.textContent = member;
                if (member.includes('(참가 중)')) {
                    li.style.color = 'grey';
                    li.style.pointerEvents = 'none';
                } else {
                    li.onclick = () => inviteMember(member.replace(' (참가 중)', ''), li);
                }
                inviteList.appendChild(li);
            });

            document.getElementById('inviteModal').style.display = 'block';
            document.getElementById('overlay').style.display = 'block';
        })
        .catch(error => {
            console.error('Failed to load members:', error);
            alert('멤버 목록을 불러오는 중 오류가 발생했습니다.');
        });
}

// 초대 모달 닫기
function closeInviteModal() {
    document.getElementById('inviteModal').style.display = 'none';
    document.getElementById('overlay').style.display = 'none';
    closeParticipantsModal();
}

// 멤버 초대
function inviteMember(memberId, element) {
    const chatroomId = document.getElementById('chatroom-data').getAttribute('data-chatroom-id');
    fetch(`/chat/invite`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ chatroomId, memberId })
    })
    .then(response => {
        if (response.ok) {
            alert(`${memberId}님이 초대되었습니다.`);
            element.classList.add('selected');
            closeInviteModal();
            closeParticipantsModal();
            sendSystemMessage(`${memberId}님이 채팅방에 초대되었습니다.`);
        } else {
            alert('초대 실패');
        }
    })
    .catch(error => {
        console.error('Failed to invite member:', error);
        alert('초대 중 오류가 발생했습니다.');
    });
}

// 시스템 메시지 전송
function sendSystemMessage(content) {
    const chatroomId = document.getElementById('chatroom-data').getAttribute('data-chatroom-id');
    const messageData = {
        chatroomId: parseInt(chatroomId),
        senderId: "System",
        messageContents: content,
        sendTime: new Date().toISOString()
    };

    if (stompClients[chatroomId]) {
        stompClients[chatroomId].send('/app/send', {}, JSON.stringify(messageData));
    }
}

// 채팅방 생성
window.createChatRoom = function () {
    const chatRoomNameInput = document.getElementById('chatRoomName');
    const createChatRoomButton = document.getElementById('createChatRoomButton');

    const chatRoomName = chatRoomNameInput.value.trim();
    if (!selectedProjectNum) {
        alert('프로젝트를 선택하세요.');
        return;
    }
    if (selectedMembers.length === 0) {
        alert('최소 한 명 이상의 팀원을 선택하세요.');
        return;
    }
    if (!chatRoomName) {
        alert('채팅방 이름을 입력하세요.');
        return;
    }

    fetch('/chat/createChatRoom', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            projectNum: selectedProjectNum,
            selectedMemberIds: selectedMembers,
            chatRoomName: chatRoomName
        })
    })
    .then(response => {
        if (response.ok) {
            alert('채팅방이 생성되었습니다.');
            updateChatRoomList();
            closeModal();
        } else {
            alert('채팅방 생성 실패');
        }
    })
    .catch(error => {
        console.error('Error creating chat room:', error);
        alert('채팅방 생성 중 오류가 발생했습니다.');
    });
};

// 모달 닫기
window.closeModal = function () {
    const modal = document.getElementById('createChatRoomModal');
    const overlay = document.getElementById('overlay');
    const chatRoomNameInput = document.getElementById('chatRoomName');

    modal.style.display = 'none';
    overlay.style.display = 'none';
    selectedProjectNum = null;
    selectedMembers = [];
    chatRoomNameInput.value = '';
    document.getElementById('teamMemberList').style.display = 'none';
    document.getElementById('chatRoomNameContainer').style.display = 'none';
    createChatRoomButton.style.display = 'none';
};

// 자동으로 채팅방 목록 업데이트 (5초마다)
setInterval(updateChatRoomList, 5000);

function openCreateChatRoomModal() {
    fetch('/chat/getProjects')
        .then(response => response.json())
        .then(data => {
            const projects = Array.isArray(data) ? data : Object.values(data);
            const projectList = document.getElementById('projects');
            projectList.innerHTML = '';
            projects.forEach(project => {
                const projectElement = document.createElement('li');
                projectElement.textContent = project.projectName; // 프로젝트 이름만 표시
                projectElement.onclick = () => selectProject(project.projectNum, projectElement);
                projectList.appendChild(projectElement);
            });

            document.getElementById('createChatRoomModal').style.display = 'block';
            document.getElementById('overlay').style.display = 'block';
        })
        .catch(error => console.error('Error fetching projects:', error));
};


function selectProject(projectNum, element) {
       const projectListItems = document.querySelectorAll('#projects li');
       projectListItems.forEach(item => item.classList.remove('selected'));
       element.classList.add('selected');
       selectedProjectNum = projectNum;

       // 선택된 프로젝트 번호를 로그로 출력하여 확인
       console.log('Selected projectNum:', selectedProjectNum);

       fetch(`/chat/getTeamMembers?projectNum=${projectNum}`)
           .then(response => response.json())
           .then(data => {
               if (!Array.isArray(data) || data.length === 0) {
                   alert('팀원 목록을 불러오지 못했습니다.');
                   return;
               }

               const memberList = document.getElementById('members');
               memberList.innerHTML = '';
               selectedMembers = [];

               data.forEach(member => {
                   const memberElement = document.createElement('li');
                   memberElement.textContent = member;
                   memberElement.onclick = () => toggleMemberSelection(member, memberElement);
                   memberList.appendChild(memberElement);
               });

               document.getElementById('teamMemberList').style.display = 'block';
           })
           .catch(error => {
               console.error('Error fetching team members:', error);
               alert('팀원 목록을 불러오는 중 오류가 발생했습니다.');
           });
   };
   
   function toggleMemberSelection(memberId, element) {
       if (selectedMembers.includes(memberId)) {
           selectedMembers = selectedMembers.filter(id => id !== memberId);
           element.classList.remove('selected');
       } else {
           selectedMembers.push(memberId);
           element.classList.add('selected');
       }

       if (selectedMembers.length > 0) {
           document.getElementById('chatRoomNameContainer').style.display = 'block';
           createChatRoomButton.style.display = 'block';
       } else {
           document.getElementById('chatRoomNameContainer').style.display = 'none';
           createChatRoomButton.style.display = 'none';
       }
   };


function downloadChat() {
    const chatroomId = document.getElementById('chatroom-data').getAttribute('data-chatroom-id');
    if (!chatroomId) {
        alert('채팅방이 선택되지 않았습니다.');
        return;
    }

    window.location.href = `/chat/downloadChat?chatroomId=${chatroomId}`;
}