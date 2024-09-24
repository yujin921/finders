// WebRTC 관련 글로벌 변수들 설정
let localStreamElement = document.querySelector('#localStream');
const myKey = Math.random().toString(36).substring(2, 11);
let pcListMap = new Map();
let roomId;
let otherKeyList = [];
let localStream = undefined;

const startCam = async () => {
    if (navigator.mediaDevices !== undefined) {
        await navigator.mediaDevices.getUserMedia({ audio: true, video: true })
            .then(async (stream) => {
                console.log('Stream found');
                localStream = stream;
                localStreamElement.srcObject = localStream;
            }).catch(error => {
                console.error("Error accessing media devices:", error);
            });
    }
};

// WebSocket 연결
const connectSocket = async () => {
    const socket = new SockJS('/ws/signaling');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect({}, function () {
        console.log('Connected to WebRTC server');

        // iceCandidate 처리
        stompClient.subscribe(`/topic/peer/iceCandidate/${myKey}/${roomId}`, candidate => {
            const key = JSON.parse(candidate.body).key;
            const message = JSON.parse(candidate.body).body;
            const pc = pcListMap.get(key);

            if (pc) {
                const iceCandidate = new RTCIceCandidate(message);
                pc.addIceCandidate(iceCandidate).catch(error => {
                    console.error('Error adding ICE candidate:', error);
                    retryIceCandidate(pc, key, iceCandidate);
                });
            }
        });

        // offer 처리
        stompClient.subscribe(`/topic/peer/offer/${myKey}/${roomId}`, offer => {
            const key = JSON.parse(offer.body).key;
            const message = JSON.parse(offer.body).body;
            pcListMap.set(key, createPeerConnection(key));

            const pc = pcListMap.get(key);
            pc.setRemoteDescription(new RTCSessionDescription({ type: message.type, sdp: message.sdp }))
                .then(() => {
                    console.log('Remote description set for', key);
                    sendAnswer(pc, key);
                }).catch(error => {
                console.error('Error setting remote description:', error);
            });
        });

        // answer 처리
        stompClient.subscribe(`/topic/peer/answer/${myKey}/${roomId}`, answer => {
            const key = JSON.parse(answer.body).key;
            const message = JSON.parse(answer.body).body;
            const pc = pcListMap.get(key);

            if (pc) {
                pc.setRemoteDescription(new RTCSessionDescription(message))
                    .catch(error => {
                        console.error('Error setting remote description:', error);
                    });
            }
        });

        // key 처리
        stompClient.subscribe(`/topic/call/key`, () => {
            stompClient.send(`/app/send/key`, {}, JSON.stringify(myKey));
        });

        // 상대방의 key를 받는 subscribe
        stompClient.subscribe(`/topic/send/key`, message => {
            const key = JSON.parse(message.body);
            if (myKey !== key && !otherKeyList.includes(key)) {
                otherKeyList.push(key);
                console.log('Received key from peer:', key);
            }
        });
    }, function (error) {
        alert('접근 권한이 없습니다. 해당 프로젝트 팀원만 접속 가능합니다.');
        console.error('WebSocket connection error:', error);
    });
};

// onTrack 이벤트 처리
let onTrack = (event, otherKey) => {
    trackReceivedTime = performance.now();
    console.log(`Track received from ${otherKey} at ${trackReceivedTime}`);

    setTimeout(() => {
        if (!document.getElementById(`${otherKey}`)) {
            const video = document.createElement('video');
            video.autoplay = true;
            video.controls = true;
            video.id = otherKey;

            if (event.streams.length > 0) {
                video.srcObject = event.streams[0];  // 스트림을 비디오 요소에 연결
                document.getElementById('remoteStreamDiv').appendChild(video);

                // 비디오 요소가 화면에 추가된 시간 기록
                const videoAddedTime = performance.now();
                console.log(`Video element created for ${otherKey} at ${videoAddedTime}`);

                // 지연 시간 계산
                const delay = videoAddedTime - trackReceivedTime;
                console.log(`Delay between track received and video element creation: ${delay.toFixed(2)} ms`);
            } else {
                console.error(`No streams available for ${otherKey}`);
            }
        }
    }, 500);   // 0.5초 지연 추가
};

// PeerConnection 생성
const createPeerConnection = (otherKey) => {
    const pc = new RTCPeerConnection();
    try {
        pc.addEventListener('icecandidate', (event) => onIceCandidate(event, otherKey));
        pc.addEventListener('track', (event) => onTrack(event, otherKey));

        if (localStream) {
            localStream.getTracks().forEach(track => {
                pc.addTrack(track, localStream);
                console.log(`Track added to PeerConnection for key: ${otherKey}`);
            });
        }

        pc.addEventListener('iceconnectionstatechange', () => {
            console.log(`ICE connection state for ${otherKey}:`, pc.iceConnectionState);

            if (pc.iceConnectionState === 'connected') {
                console.log(`ICE connection established for ${otherKey}`);
            } else if (pc.iceConnectionState === 'failed') {
                console.log(`ICE connection failed for ${otherKey}, retrying...`);
                retryConnection(pc, otherKey);  // 실패 시 재시도
            }
        });

        console.log(`PeerConnection created for key: ${otherKey}`);
    } catch (error) {
        console.error('PeerConnection failed:', error);
    }
    return pc;
};

// ICE candidate 처리
let onIceCandidate = (event, otherKey) => {
    if (event.candidate) {
        console.log('ICE candidate found:', event.candidate);
        stompClient.send(`/app/peer/iceCandidate/${otherKey}/${roomId}`, {}, JSON.stringify({
            key: myKey,
            body: event.candidate
        }));
    }
};

// Offer 전송
let sendOffer = (pc, otherKey) => {
    pc.createOffer().then(offer => {
        setLocalAndSendMessage(pc, offer);
        stompClient.send(`/app/peer/offer/${otherKey}/${roomId}`, {}, JSON.stringify({
            key: myKey,
            body: offer
        }));
        console.log('Offer sent to', otherKey);
    }).catch(error => {
        console.error('Error creating offer:', error);
    });
};

// Answer 전송
let sendAnswer = (pc, otherKey) => {
    pc.createAnswer().then(answer => {
        setLocalAndSendMessage(pc, answer);
        stompClient.send(`/app/peer/answer/${otherKey}/${roomId}`, {}, JSON.stringify({
            key: myKey,
            body: answer
        }));
        console.log('Send answer');
    });
};

// 로컬 SDP 설정 및 전송
const setLocalAndSendMessage = (pc, sessionDescription) => {
    pc.setLocalDescription(sessionDescription).catch(error => {
        console.error('Error setting local description:', error);
    });
};

// 룸 번호 입력 후 소켓 연결
document.querySelector('#enterRoomBtn').addEventListener('click', async () => {
    await startCam();

    if (localStream !== undefined) {
        document.querySelector('#localStream').style.display = 'block';
        document.querySelector('#startSteamBtn').style.display = '';
    }
    roomId = document.querySelector('#roomIdInput').value;
    document.querySelector('#roomIdInput').disabled = true;
    document.querySelector('#enterRoomBtn').disabled = true;

    await connectSocket();
});

// 스트림 시작 버튼
document.querySelector('#startSteamBtn').addEventListener('click', async () => {
    stompClient.send(`/app/call/key`, {}, {});

    setTimeout(() => {
        otherKeyList.forEach((key) => {
            if (!pcListMap.has(key)) {
                pcListMap.set(key, createPeerConnection(key));
                sendOffer(pcListMap.get(key), key);
            }
        });
    }, 1000);  // 지연 시간을 조정하여 빠르게 처리
});

let retryConnection = (pc, otherKey) => {
    setTimeout(() => {
        console.log(`Retrying connection for ${otherKey}`);
        pc.restartIce();  // ICE 연결 재시도
    }, 1000);  // 1초 뒤 재시도
};