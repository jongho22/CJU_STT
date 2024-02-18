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
     <img id="loding_gif" src='<c:url value="/resources/img/loding.gif"/>' width="100" style="display: none;">
    </form>
    <br>
    <p id="apiGuideText" style="color:DBE7C9">API 사용시 연구실 서버를 사용하지 않아 더 빠른 변환을 할 수 있습니다.</p>
    
    <textarea class="form-control" id="typed-text" rows="10" readonly style="display: none;"></textarea>
  </main>
  <%@ include file="footer.jsp" %>
</div>

<script>
	var socket = new WebSocket("ws://203.252.230.243:8090/cju_stt/audio");
	
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
		$('#loding_gif').hide();
		$('#apiGuideText').hide();
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
		$('#service_title').text("작업 진행중");
		$('#explan_text').text("파일 용량이 크면 오래걸리니 기다려주세요.");
		$('#loding_gif').show();

		const formData = new FormData(this);
		const apiCheck = $('#apiCheck').val();

		if (apiCheck == "false") {
			console.log("서버 사용");
			$.ajax({
				url : "/cju_stt/upload",
				type : "POST",
				data : formData,
				processData : false,
				contentType : false,
				success : function(data) {
					changePage();
					var result = JSON.parse(data);
					typeEffect(result.result, 50);
					console.log("변환 성공");
				},
				error : function() {
					console.log("AJAX 호출 실패");
				}
			});
		} else {

			console.log("API 사용");
			var fileInput = document.getElementById("fileInput");
			var file = fileInput.files[0];
			var fileName = file.name;
			
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
		console.log("WebSocket 연결 되었습니다!")
	}

	socket.onmessage = function(event) {
		typeEffect(event.data, 50);
	};

	socket.onclose = function(event) {
		console.log('WebSocket 연결이 닫혔습니다.');
		$('#service_title').text("음성파일 변환 완료");
	}

	/* socket.onerror = function(error) {
		console.error("WebSocket 오류 발생: " + error.message);
	}; */
</script>

</body>
</html>
