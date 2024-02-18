<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<html>
<%@ include file="head.jsp" %>

<body>
	<p>웹 소켓 테스트 페이지</p>
	<form id="uploadForm" enctype="multipart/form-data">
        <input type="file" id="audioFile" name="audioFile">
        <button type="button" onclick="uploadAudio()">전송</button>
    </form>
</body>
<script>
	var socket = new WebSocket("ws://203.252.230.243:8090/cju_stt/audio");
	
	// 음성 파일 전송
	function uploadAudio() {
		     console.log("전송 시작.");
            var fileInput = document.getElementById("audioFile");
            var file = fileInput.files[0];
            var fileName = file.name;
            
            // FileReader를 사용하여 파일을 읽음
            var reader = new FileReader();
            reader.onload = function(event) {
                var arrayBuffer = event.target.result;
                
                // ArrayBuffer를 WebSocket을 통해 전송\
                socket.send(fileName);
                socket.send(arrayBuffer);
                console.log("전송 완료.");
            };
            reader.readAsArrayBuffer(file);
        }
	
	socket.onopen = function(event) {
		console.log("WebSocket 연결 되었습니다!")
	}
	
	socket.onmessage = function(event) {
	    console.log("서버로부터 메시지를 수신했습니다: " + event.data);
	};
	
	socket.onclose = function(event) {
		console.log('WebSocket 연결이 닫혔습니다.');
	}
	
	socket.onerror = function(error) {
	    console.error("WebSocket 오류 발생: " + error.message);
	};
</script>
</html>
