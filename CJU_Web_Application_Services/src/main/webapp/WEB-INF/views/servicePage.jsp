<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>

<html>
<!-- 한글 적용 -->
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="head.jsp" %>

<body class="d-flex h-100 text-center text-bg-dark">

<div class="cover-container d-flex w-100 h-100 p-3 mx-auto flex-column">
  <%@ include file="header.jsp" %>

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

  <%@ include file="footer.jsp" %>
  
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
