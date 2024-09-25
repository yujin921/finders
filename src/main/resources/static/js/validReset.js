$(document).ready(function() {
	$("#resetPassword").submit(check1);
});

function check1() {
	let pw = $("#newPassword").val();
	let pw2 = $("#confirmPassword").val();

	if (pw.length < 8 || pw.length > 20) {
		alert("PW는 8자 이상 20자 이하의 글자를 반드시 입력해주세요!!");
		$("#newPassword").focus();
		$("#confirmPassword").val('');
		
		return false;
	}
	
	if (pw != pw2) {
		alert("입력하신 비밀번호가 일치하지 않습니다.\n확인 후 다시 입력해주세요!!");
		$("#newPassword").focus();
		$("#confirmPassword").val('');
		
		return false;
	}
	
	return true;
}

function check2() {
	let pw = $("#client-password").val();
	let pw2 = $("#client-password-check").val();
	let name = $("#client-name").val();
	
	if (pw.length < 8 || pw.length > 20) {
		alert("PW는 8자 이상 20자 이하의 글자를 반드시 입력해주세요!!");
		$("#client-password").focus();
		$("#client-password").val('');
		
		return false;
	}
	
	if (pw != pw2) {
		alert("입력하신 비밀번호가 일치하지 않습니다.\n확인 후 다시 입력해주세요!!");
		$("#client-password-check").focus();
		$("#client-password-check").val('');
		
		return false;
	}
	
	return true;
}

function winOpen() {
	// window.open() : 새창을 띄우는 역할을 함
	let w = window.open('idCheck', 'win', 'left=500,top=200,width=500,height=400,location=no');
}