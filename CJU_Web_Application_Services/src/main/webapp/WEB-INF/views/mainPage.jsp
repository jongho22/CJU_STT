<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page session="false" %>

<style>
	#guideScroll {
		padding-top:100pt;
		width : 30%;
		margin: 0 auto;
		transition: all linear 0.3s;
	}
	#guideScroll:hover {
		transform : scale(1.1);
	}
	#guideText {
		color:black; 
		text-align:left; 
		padding-left:10pt;
	}
	#guideBox {
		background-color: EEEDEB;
		padding:2px 30px 2px 30px;
		margin: 0pt 100pt 10pt 100pt;
	}
</style>

<html>
<%@ include file="head.jsp" %>
<body class="d-flex h-100 text-center text-bg-dark">
<div class="cover-container d-flex w-100 h-100 mx-auto flex-column" style="margin:5pt">
   <%@ include file="header.jsp" %>
  <main class="px-3">
  	<div style="padding-top: 187pt;">
	 	<h1 class="">회의 음성기록 텍스트 변환</h1>
	    <p class="lead">간편한 사용법과 정확한 변환 기술로, 음성메모나 파일을 텍스트 변환하여 필요한 정보를 빠르게 얻을 수 있습니다.</p>
	    <p class="lead">
	      <a id="button" href="service" class="btn btn-lg btn-light fw-bold border-white">텍스트 변환</a>
	    </p>
  	</div>
  	<div id="guideScroll">
  		<h3>사용 방법이 궁금하신가요?</h3>
		<img src="<c:url value='/resources/img/down.png'/>" width="30"/>
  	</div>
  	<div style="margin: 40% 0 5% 0;">
	  	<h1>사용 방법 및 안내사항</h1>
	  	<br>
	  	<div id="guideBox" class="rounded">
		    <div style="margin:40pt;">
		    	<h3 style="color:black; text-align:left">사용 방법</h3>
			    <hr style="color:black">
		 	    <p id="guideText"><b>1. 텍스트 '변환 버튼'을 클릭해주세요.</b></p>
		 	    <p id="guideText"><b>2. 변환할 음성 파일을 선택해주세요.</b></p>
		 	    <p id="guideText"><b>3. '변환버튼'을 누르면 작업이 시작됩니다.</b></p>
		 	    <br>
		 	    <h3 style="color:black; text-align:left;">안내 사항</h3>
		 	    <hr style="color:black">
		 	    <div class="row">
			 	    <div class="col-6">
			 	    	<p id="guideText"><b>- 공통</b></p>
			 	    	<p id="guideText"><b>1. 현재 .mp3 확장자만 변환 가능합니다.</b></p>
			 	    	<p id="guideText"><b>2. 업로드하는 음성 파일은 서버에 저장되지 않습니다. (변환 즉시 삭제)</b></p>
			 	    	<p id="guideText"><b>3. 작업 속도 : (서버 : 1시간 음성 파일 = 약 10분)</b></p>
			 	    	<p id="guideText"><b>4. 파일명에 띄어쓰기가 들어가면 안됩니다.</b></p>
			 	    	<br>
			 	    	<p id="guideText"><b>- API 사용시</b></p>
			 	    	<p id="guideText"><b>1. 하루 사용량에 제한이 있어, 많은 변환 요청시 서비스가 종료될 수 있습니다.</b></p>
			 	    </div>
		 	    </div>
		    </div>
	  	</div>
	  </div>
  </main>
  <%@ include file="footer.jsp" %>
</div>
</body>
<script>
	$("#guideScroll").click(function(){
		var offset = $("#guideBox").offset(); 
		$("html, body").animate({scrollTop: offset.top},300);
	});
</script>
</html>
