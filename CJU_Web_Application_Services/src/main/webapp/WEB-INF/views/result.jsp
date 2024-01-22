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
	
	<style>
		#typed-text {
		white-space:nowrap;
		overflow:hidden;
		border-right:2px solid #000;
		padding-right: 8px;
		}
	</style>
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
    <h1>변환 결과</h1>
    <p id="typed-text">${message}</p>
    <div id="loadingMessage"></div>
  </main>

	
  <footer class="mt-auto text-white-50">
    <p>청주대학교 인공지능소프트웨어 <a href="http://acin.cju.ac.kr/" class="text-white">ACIN연구실</a>, Developed by 신종호.</p>
  </footer>
</div>

<script>
	function typeEffect(element, speed){
	 const text = element.innerHTML;
	 element.innerHTML = '';
	 
	 let i = 0;
	 const typingInterval = setInterval(function() {
		 element.innerHTML += text.charAt(i);
		 i++;
		 
		 if (i> text.lenght) {
			 clearInterval(typingInterval);
		 }
	 }, speed);
	}
	
	window.onload = function() {
		const typedTextElement = document.getElementById('typed-text');
		typeEffect(typedTextElement,50);
	};
	
</script>

<script>
	$(document).ready(function () {
	    $.ajax({
	        url: "/cju_stt/upload",  // 서버의 엔드포인트
	        type: "GET",
	        success: function (data) {
	            // 서버로부터 받은 JSON 데이터를 파싱하여 화면에 표시
	            var result = JSON.parse(data);
	            $("#result").text(result.result);
	            console.log(result);
	        },
	        error: function () {
	            console.log("서버와 통신 중 오류 발생");
	        }
	    });
	});
</script>

</body>
</html>
