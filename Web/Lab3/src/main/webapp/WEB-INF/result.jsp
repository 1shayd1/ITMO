<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="en_US"/>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="styles.css">
    <title>Result</title>
</head>
<body>
<header>Shalabodov Yaroslav Dmitrievich, Р3210, Variant 32452</header>

<div class="result-card">
    <table class="result-table">
        <tr><th>X</th><td><fmt:formatNumber value="${result.x}" minFractionDigits="2" maxFractionDigits="2"/></td></tr>
        <tr><th>Y</th><td><fmt:formatNumber value="${result.y}" minFractionDigits="2" maxFractionDigits="2"/></td></tr>
        <tr><th>R</th><td><fmt:formatNumber value="${result.r}" maxFractionDigits="0"/></td></tr>
        <tr><th>Result</th>
            <c:choose>
                <c:when test="${result.hit}"><td class="hit">Hit</td></c:when>
                <c:otherwise><td class="miss">Miss</td></c:otherwise>
            </c:choose>
        </tr>
        <tr><th>Time</th><td>${result.time}</td></tr>
        <tr><th>Working time</th><td>${result.workTime}</td></tr>
    </table>
    <a class="back-link" href="controller">← Back to form</a>
</div>
</body>
</html>