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
    <h1>회의 음성기록 텍스트 변환</h1>
    <p class="lead">간편한 사용법과 정확한 변환 기술로, 음성메모나 파일을 텍스트 변환하여 필요한 정보를 빠르게 얻을 수 있습니다.</p>
    <p class="lead">
      <a href="service" class="btn btn-lg btn-light fw-bold border-white bg-white">텍스트 변환</a>
    </p>
  </main>

  <%@ include file="footer.jsp" %>
</div>

</body>
</html>
