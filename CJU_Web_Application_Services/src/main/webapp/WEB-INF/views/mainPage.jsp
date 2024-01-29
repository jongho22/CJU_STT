<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page session="false" %>

<html>
<%@ include file="head.jsp" %>

<body class="d-flex h-100 text-center text-bg-dark">

<div class="cover-container d-flex w-100 h-100 p-3 mx-auto flex-column">
   <%@ include file="header.jsp" %>

  <main class="px-3">
  
  	<div style="padding-top: 187pt;">
	 	<h1 class="">회의 음성기록 텍스트 변환</h1>
	    <p class="lead">간편한 사용법과 정확한 변환 기술로, 음성메모나 파일을 텍스트 변환하여 필요한 정보를 빠르게 얻을 수 있습니다.</p>
	    <p class="lead">
	      <a id="button" href="service" class="btn btn-lg btn-light fw-bold border-black ">텍스트 변환</a>
	    </p>
  	</div>
  	<h3 id="guide_scroll" style="padding-top:100pt">사용 방법이 궁금하신가요?</h3>
  	<img class="down" src="<c:url value='/resources/img/down.png'/>" width="30"/>
  	
  	<div style="margin: 40% 0 5% 0;">
	  	<h1>사용 방법 및 안내사항</h1>
	  	<br>
	  	<div id="guide_text" class="rounded" style="background-color: EEEDEB; padding:2px 30px 2px 30px">
		    <div style="margin:40pt;">
		    	<h3 style="color:black; text-align:left">사용 방법</h3>
			    <hr style="color:black">
		 	    <p style="color:black; text-align:left; padding-left:10pt;"><b>1. 텍스트 '변환 버튼'을 클릭해주세요.</b></p>
		 	    <p style="color:black; text-align:left; padding-left:10pt;"><b>2. 변환할 음성 파일을 선택해주세요.</b></p>
		 	    <p style="color:black; text-align:left; padding-left:10pt;"><b>3. '변환버튼'을 누르면 작업이 시작됩니다.</b></p>
		 	    <br>
		 	    <h3 style="color:black; text-align:left;">안내 사항</h3>
		 	    <hr style="color:black">
		 	    <div class="row">
			 	    <div class="col-6">
			 	    	<p style="color:black; text-align:left; padding-left:10pt;"><b>1. 현재 .mp3 확장자만 변환 가능합니다.</b></p>
			 	    	<p style="color:black; text-align:left; padding-left:10pt;"><b>2. 업로드하는 음성 파일은 서버에 저장되지 않습니다.</b></p>
			 	    	<p style="color:black; text-align:left; padding-left:10pt;"><b>3. 작업 속도 : (1시간 음성 파일 = 약 10분)</b></p>
			 	    </div>
			 	    <div class="col-6">
			 	       <br>
			 	    	<img id="nica_logo" src="<c:url value='/resources/img/nica.png'/>"  width="30%"/>
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
	$("#guide_scroll").click(function(){
		var offset = $("#guide_text").offset(); 
		$("html, body").animate({scrollTop: offset.top},300);
	});
	
	$(document).ready(function () {
		$("#guide_scroll").mouseover(function(){
			$("#guide_scroll").css("color", "A9A9A9");
		}).mouseleave(function(){
			$("#guide_scroll").css("color", "white");
		});
		
		$("#nica_logo").mouseover(function(){
		  	$("#nica_logo").attr("src", "<c:url value='/resources/img/nica_click.png'/>"); 
		}).mouseleave(function(){
			$("#nica_logo").attr("src", "<c:url value='/resources/img/nica.png'/>"); 
		}).click(function() {
			$(location).attr("href", "http://acin.cju.ac.kr/");
		});
		
	});
</script>
</html>
