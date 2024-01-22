<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>

<html>

<head>
	<!-- 한글 적용 -->
	<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
	
	<!-- 부트스트랩 적용 -->
	<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN" crossorigin="anonymous">
	<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js" integrity="sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL" crossorigin="anonymous"></script>
	
	<!-- Jquery -->
	<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.6.0/jquery.min.js" integrity="sha512-894YE6QWD5I59HgZOGReFYm4dnWc1Qt5NtvYSaNcOP+u1T9qYdvdihz0PPSiiqn/+/3e7Jo4EaG7TubfWGUrMQ==" crossorigin="anonymous" referrerpolicy="no-referrer"></script> 
	
	<title>청주대학교 STT 서비스</title>
	
</head>

<body class="d-flex h-100 text-center text-bg-dark">

<div class="cover-container d-flex w-100 h-100 p-3 mx-auto flex-column">
  <header class="mb-auto">
    <div>
      <h3 class="float-md-start mb-0">청주대학교 STT 서비스</h3>
      <nav class="nav nav-masthead justify-content-center float-md-end">
        <a class="nav-link fw-bold py-1 px-0 active" style="color:white" href="/cju_stt">Home</a>
        &nbsp; &nbsp;
        <a class="nav-link fw-bold py-1 px-0" style="color:white" href="https://www.cju.ac.kr/www/index.do">청주대학교</a>
        &nbsp; &nbsp;
        <a class="nav-link fw-bold py-1 px-0" style="color:white" href="http://acin.cju.ac.kr/">ACIN연구실</a>
      </nav>
    </div>
  </header>

  <main class="px-3">
    <h1 id="service_title">변환을 시작합니다!</h1>
    <!-- action="<c:url value='/upload'/>" 폼 저장용 -->
    <hr>
    <form id="uploadForm" method="post" enctype="multipart/form-data">
     <div class="container" style="width:40%">
		<label for="formFile" class="form-label">변환하실 음성파일을 넣어주세요.</label>
		<input class="form-control" type="file" name="file">
	  </div>
	  </br>
     <input type="submit" class="btn btn-lg btn-light fw-bold border-white bg-white" value="변환 시작" />
    </form>
    
    <textarea class="form-control" id="typed-text" rows="3" style="display: none;"></textarea>
  
  </main>

	
  <footer class="mt-auto text-white-50">
    <p>청주대학교 인공지능소프트웨어 <a href="http://acin.cju.ac.kr/" class="text-white">ACIN연구실</a>, Developed by 신종호.</p>
  </footer>
</div>

<script>
	function typeEffect(text, speed){
		 const element = document.getElementById('typed-text');
	  	 let i = 0;
	  	 const typingInterval = setInterval(function() {
	  		 element.innerHTML += text.charAt(i);
	  		 i++;
	  		 
	  		 if (i> text.lenght) {
	  			 clearInterval(typingInterval);
	  		 }
	  	 }, speed);
	 }
	
	function changePage() {
		$('#service_title').text("변환 결과");
		$('#uploadForm').remove();
		$('#typed-text').show();
	}
	
	$(document).ready(function () {
		$("#uploadForm").submit(function (event) {
			event.preventDefault();
			 console.log("check");
	        var formData = new FormData(this);

	        $.ajax({
	            url: "/cju_stt/upload",
	            type: "POST",
	            data: formData,
	            processData: false,
	            contentType: false,
	            success: function (data) {
	            	  changePage();
	                var result = JSON.parse(data);
		            typeEffect(result.result, 50);
		            console.log("변환 성공!!");
	            },
	            error: function (jqXHR, textStatus, errorThrown) {
	            	console.log("AJAX 호출 실패");
	                console.log("상태 코드: " + jqXHR.status);
	                console.log("에러 타입: " + textStatus);
	                console.log("에러 내용: " + errorThrown);
	            }
	        });
		});
    });
</script>


</body>
</html>
