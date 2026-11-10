<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="styles.css">
    <script type="module" src="script.js"></script>
    <title>Web Lab 3</title>
</head>
<body>
<header>
    Shalabodov Yaroslav Dmitrievich, Р3210, Variant 32452
</header>

<div class="main">
    <div class="left-section">
        <form class="input-block" action="controller" method="get">
            <div>
                <span>Field X</span>
                <c:forEach var="i" begin="0" end="8">
                    <button type="button" class="x-btn" value="${i - 4}">${i - 4}</button>
                </c:forEach>
                <input type="hidden" name="x" id="x-input">
            </div>

            <div>
                <label for="y-text">Field Y</label>
                <input type="text" id="y-text" name="y" placeholder="Y ∈ (-5; 5)">
            </div>

            <div>
                <label for="r-select">Field R</label>
                <select id="r-select" name="r">
                    <option value="1.0">1.0</option>
                    <option value="2.0">2.0</option>
                    <option value="3.0" selected>3.0</option>
                    <option value="4.0">4.0</option>
                    <option value="5.0">5.0</option>
                </select>
            </div>

            <button type="submit">Check</button>
            <div id="error"></div>
        </form>

        <div class="canvas-section">
            <canvas height="460" width="460" id="canvas"></canvas>
        </div>
    </div>

    <div class="table-right">
        <div class="table-scroll">
            <table>
                <caption>Target shot stats</caption>
                <thead>
                <tr>
                    <th>X</th><th>Y</th><th>R</th><th>Is Hit</th><th>Date, Time</th><th>Working Time</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="res" items="${resultBean.results}">
                    <tr data-x="${res.x}" data-y="${res.y}" data-r="${res.r}" data-hit="${res.hit}">
                        <td>${res.x}</td>
                        <td>${res.y}</td>
                        <td>${res.r}</td>
                        <td>${res.hit}</td>
                        <td>${res.time}</td>
                        <td>${res.workTime}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
        <a class="back-link" href="controller?clear=true">Clear table</a>
    </div>
</div>
</body>
</html>