document.addEventListener("DOMContentLoaded", () => {
    const startCallButton = document.getElementById('startCall');
    const localVideo = document.getElementById('localVideo');
    const remoteVideo = document.getElementById('remoteVideo');

    if (startCallButton) {
        startCallButton.addEventListener('click', startCall);
    }

    let localStream;
    let peerConnection;
    let iceCandidatesQueue = []; // ICE 후보를 임시 저장하는 큐
    const configuration = {
        iceServers: [{ urls: 'stun:stun.l.google.com:19302' }] // STUN 서버 설정
    };

    const signalingSocket = new SockJS("/ws/signaling");
    const stompClient = Stomp.over(signalingSocket);

    stompClient.connect({}, function (frame) {

        stompClient.subscribe("/topic/signaling", function (message) {
            const data = JSON.parse(message.body);

            if (!peerConnection) {
                peerConnection = new RTCPeerConnection(configuration);
            }

            if (data.sdp && data.sdp.type === 'offer') {
                peerConnection.setRemoteDescription(new RTCSessionDescription(data.sdp))
                    .then(async () => {
                        const answer = await peerConnection.createAnswer();
                        await peerConnection.setLocalDescription(answer);

                        // ICE 후보 처리
                        iceCandidatesQueue.forEach(candidate => peerConnection.addIceCandidate(candidate));
                        iceCandidatesQueue = [];

                        stompClient.send("/app/signaling", {}, JSON.stringify({ sdp: peerConnection.localDescription }));
                    })
                    .catch(error => console.error("Error setting remote offer:", error));
            } else if (data.sdp && data.sdp.type === 'answer') {
                if (peerConnection.signalingState === 'have-local-offer') {
                    peerConnection.setRemoteDescription(new RTCSessionDescription(data.sdp))
                        .then(() => {
                            iceCandidatesQueue.forEach(candidate => peerConnection.addIceCandidate(candidate));
                            iceCandidatesQueue = [];
                        })
                        .catch(error => console.error("Error setting remote answer:", error));
                }
            } else if (data.ice) {
                if (peerConnection.remoteDescription) {
                    peerConnection.addIceCandidate(new RTCIceCandidate(data.ice))
                        .catch(error => console.error("Error adding received ICE candidate:", error));
                } else {
                    iceCandidatesQueue.push(new RTCIceCandidate(data.ice));
                }
            }
        });
    });

    async function startCall() {
        localStream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });
        document.getElementById('localVideo').srcObject = localStream;

        peerConnection = new RTCPeerConnection(configuration);

        // 로컬 트랙을 추가 (자신의 비디오)
        localStream.getTracks().forEach(track => peerConnection.addTrack(track, localStream));

        peerConnection.onicecandidate = (event) => {
            if (event.candidate) {
                stompClient.send("/app/signaling", {}, JSON.stringify({ ice: event.candidate }));
            }
        };

        // 상대방 트랙 수신 (원격 비디오)
        peerConnection.ontrack = (event) => {
            document.getElementById('remoteVideo').srcObject = event.streams[0];
        };

        // Offer 생성 및 송신
        const offer = await peerConnection.createOffer();
        await peerConnection.setLocalDescription(offer);

        stompClient.send("/app/signaling", {}, JSON.stringify({ sdp: peerConnection.localDescription }));
    }
});
