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
		<input class="form-control" type="file" name="file">
	  </div>
	  </br>
     <input id="submit_button" type="submit" class="btn btn-lg btn-light fw-bold border-white" value="변환 시작" />
     <img id="loding_gif" src='<c:url value="/resources/img/loding.gif"/>' width="100" style="display: none;">
    </form>
    
    <textarea class="form-control" id="typed-text" rows="10" readonly style="display: none;"></textarea>
  
  </main>
  <%@ include file="footer.jsp" %>
</div>

<script>
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
		$('#service_title').text("음성파일 변환 결과");
		$('#uploadForm').remove();
		$('#typed-text').show();
		$('#loding_gif').hide();
	}
	
	$(document).ready(function () {
		$("#uploadForm").submit(function (event) {
			 event.preventDefault();
			 $('#submit_button').remove();
			 $('#service_title').text("작업 진행중");
			 $('#explan_text').text("파일 용량이 크면 오래걸리니 기다려주세요.");
			 $('#loding_gif').show();
			 
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
		            console.log("변환 성공");
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
