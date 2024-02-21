<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<html>
<%@ include file="head.jsp" %>

<body class="d-flex h-100 text-center text-bg-dark">

<div class="cover-container d-flex w-100 h-100 mx-auto flex-column" style="margin:5pt">
  <%@ include file="header.jsp" %>

  <main class="px-3">
  	 
	 <h1 id="service_title">변환을 시작합니다!</h1>
	 <br>
  	 <div class="progress" id="progress" style="display: none;">
	  <div class="progress-bar" id="progressbar" role="progressbar" style="width: 0%;" aria-valuenow="25" aria-valuemin="0" aria-valuemax="100">0%</div>
	 </div>
  	 
    <form id="uploadForm" method="post" enctype="multipart/form-data">
     <div class="container" style="width:40%">
		<label id="explan_text" for="formFile" class="form-label">변환하실 음성파일을 넣어주세요.</label>
		<input class="form-control" id="fileInput" name="fileInput" type="file">
	  </div>
	  <br>
	  <div style="width:20%; margin:0 auto;">
   		<label class="form-check-label">API 서버 사용 여부</label>
   		&nbsp;
   		<input id="apiCheck" class="form-check-input" type="checkbox" value="false">
	 </div>
	 <br>
     <input id="submit_button" type="submit" class="btn btn-lg btn-light fw-bold border-white" value="변환 시작" />
    </form>
    <br>
    <p id="apiGuideText" style="color:DBE7C9">API 변환 방식은 사용횟수가 제한되어있습니다. </p>
     
    <textarea class="form-control" id="typed-text" rows="10" readonly style="display: none;"></textarea>
  </main>
  <%-- <img id="loding_gif" src='<c:url value="/resources/img/loding.gif"/>' width="100" style="display:none;"/> --%>
  <%@ include file="footer.jsp" %>
</div>

<script>
	var socket = new WebSocket("ws://203.252.230.243:8090/cju_stt/uploadToAPI");
	var socketToServer = new WebSocket("ws://203.252.230.243:8090/cju_stt/uploadToServer");
	
	function typeEffect(text, speed){
		 const element = document.getElementById('typed-text');
	  	 let i = 0;
	  	 const typingInterval = setInterval(function() {
	  		 element.innerHTML += text.charAt(i);
	  		 i++;
	  		 if (i> text.length) {
	  			 clearInterval(typingInterval);
	  		 }
	  	 }, speed);
	 }
	
	function changePage() {
		$('#service_title').text("음성파일 실시간 변환중...");
		$('#uploadForm').remove();
		$('#typed-text').show();
		$('#apiGuideText').hide();
		$('#loding_gif').show();
		$('#progress').show();
	}
	
	$('#apiCheck').click(function(){
		const checkButton = $('#apiCheck').val();
		if (checkButton == "false") {
			$('#apiCheck').val("true");
		} else {
			$('#apiCheck').val("false");
		}
	});

	$("#uploadForm").submit(function(event) {
		event.preventDefault();
		$('#submit_button').remove();

		const formData = new FormData(this);
		const apiCheck = $('#apiCheck').val();
		
		var fileInput = document.getElementById("fileInput");
		var file = fileInput.files[0];
		var fileName = file.name;
		
		if (apiCheck == "false") {
			//console.log("서버 사용");
			changePage();
			
			// 서버를 사용 할 때 접속할 소켓 작성
			var reader = new FileReader();
			reader.onload = function(event) {
				var arrayBuffer = event.target.result;
				socketToServer.send(fileName);
				socketToServer.send(arrayBuffer);
			};
			reader.readAsArrayBuffer(file);
			
		} else {
			//console.log("API 사용");
			changePage();
		
			var reader = new FileReader();
			reader.onload = function(event) {
				var arrayBuffer = event.target.result;
				socket.send(fileName);
				socket.send(arrayBuffer);
			};
			reader.readAsArrayBuffer(file);
		}
	});

	socket.onopen = function(event) {
		//console.log("WebSocketToAPI 연결 되었습니다!")
	}

	socket.onmessage = function(event) {
		try {
			var jsonData = JSON.parse(event.data); 
		    var percent = jsonData.percent;
		    var resultText = jsonData.resultText;
		    	
		    typeEffect(resultText, 20);
		    $("#progressbar").css("width", percent + "%").text(percent + "%");
		} catch {
			typeEffect(event.data, 5);
		}
	};

	socket.onclose = function(event) {
		//console.log('WebSocketToAPI 연결이 닫혔습니다.');
		$('#service_title').text("음성파일 변환 완료");
		$('#loding_gif').hide();
		socketToServer.close();
	}
	
	socketToServer.onopen = function(event) {
		//console.log("WebSocketToServer 연결 되었습니다!")
	}

	socketToServer.onmessage = function(event) {
		try {
			var jsonData = JSON.parse(event.data); 
		    var percent = jsonData.percent;
		    var resultText = jsonData.resultText;
		    	
		    typeEffect(resultText, 5);
		    $("#progressbar").css("width", percent + "%").text(percent + "%");
		} catch {
			typeEffect(event.data, 5);
		}
	};

	socketToServer.onclose = function(event) {
		//console.log('WebSocketToServer 연결이 닫혔습니다.');
		$('#service_title').text("음성파일 변환 완료");
		$('#loding_gif').hide();
		socket.close();
	}
</script>

</body>
</html>
