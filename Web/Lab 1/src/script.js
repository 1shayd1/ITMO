window.onload = function () {
    const canvas = document.getElementById('canvas');
    const ctx = canvas.getContext('2d');

    const form = document.querySelector('.input-block');

    // canvas settings
    const width = canvas.width;
    const height = canvas.height;
    const centerX = width / 2;
    const centerY = height / 2;
    const arrowLength = 4;
    const startAngle = Math.PI / 180 * 270
    const endAngle = 0;

    const radios = document.querySelectorAll('input[name="radio"]');
    let currentR = 3 * 40;

    drawPicture(width, height, centerX, centerY, arrowLength, ctx, currentR, null, null, startAngle, endAngle);

    radios.forEach(radio => {
        radio.addEventListener('change',() => {
            const newR = document.querySelector('input[name="radio"]:checked').value * 40;
            if (newR !== currentR) {
                currentR = newR;
                drawPicture(width, height, centerX, centerY, arrowLength, ctx, currentR, null,null, startAngle, endAngle);
            }
        });
    })

    form.addEventListener('submit', (e) => {
        e.preventDefault();
        const checkX = parseFloat(document.getElementById('x-select').value);
        const checkY_1 = document.getElementById('y-text').value;
        const errorMessage = document.getElementById("y-error");

        if (!twoDigitsAfterDot(checkY_1)) {
            errorMessage.textContent = 'Y должен быть числом и содержать максимум 2 знака после запятой';
            drawPicture(width, height, centerX, centerY, arrowLength, ctx, currentR, null, null, startAngle, endAngle);
            return;
        }

        const checkY = parseFloat(checkY_1);

        if (!correctNumber(checkY)) {
            errorMessage.textContent = 'Y должен быть числом и принадлежать (-3;3)';
            drawPicture(width, height, centerX, centerY, arrowLength, ctx, currentR, null, null, startAngle, endAngle);
            return;
        }


        const X = centerX + checkX * 40;
        const Y = centerY - checkY * 40;
        errorMessage.textContent = "";
        drawPicture(width, height, centerX, centerY, arrowLength, ctx, currentR, X, Y, startAngle, endAngle);
    })
}


function drawPicture(width, height, centerX, centerY, arrowLength, ctx, r, X, Y, startAngle, endAngle) {

    ctx.clearRect(0, 0, width, height);

    drawFigures(ctx, centerX, centerY, r, startAngle, endAngle);

    drawAxes(ctx, width, height, arrowLength, centerX, centerY);

    drawFigureLines(ctx, centerX, centerY, r, startAngle, endAngle);

    drawStripes(ctx, centerX, centerY, arrowLength);

    drawCenterDot(ctx, centerX, centerY);

    drawText(ctx, centerX, centerY, r, width);

    drawDot(ctx, X, Y);
}

function drawFigures(ctx, centerX, centerY, r, startAngle, endAngle) {
    //circle-quater
    ctx.beginPath();
    ctx.moveTo(centerX, centerY);
    ctx.arc(centerX, centerY, r, startAngle, endAngle, false);

    ctx.fillStyle = "#00FFFF";
    ctx.fill();

    //triange
    ctx.beginPath();
    ctx.moveTo(centerX + r/2, centerY);
    ctx.lineTo(centerX, centerY + r);
    ctx.lineTo(centerX, centerY);

    ctx.fillStyle = "#00FFFF";
    ctx.fill();

    //rectangle
    ctx.beginPath();
    ctx.moveTo(centerX, centerY);
    ctx.lineTo(centerX - r, centerY);
    ctx.lineTo(centerX -r, centerY - r/2);
    ctx.lineTo(centerX, centerY - r/2);

    ctx.fillStyle = "#00FFFF";
    ctx.fill();
}

function drawAxes(ctx, width, height, arrowLength, centerX, centerY) {
    //Ox
    ctx.beginPath();
    ctx.moveTo(0, centerY);
    ctx.lineTo(width, centerY);
    ctx.strokeStyle = '#1A202C';
    ctx.lineWidth = 1.5;
    ctx.stroke();

    ctx.beginPath();
    ctx.moveTo(width, centerY);
    ctx.lineTo(width - 2*arrowLength, centerY - arrowLength);
    ctx.lineTo(width - 2*arrowLength, centerY + arrowLength);
    ctx.fillStyle = "#1A202C";
    ctx.fill();

    //Oy
    ctx.beginPath();
    ctx.moveTo(centerX, height);
    ctx.lineTo(centerX, 0);
    ctx.strokeStyle = '#1A202C';
    ctx.lineWidth = 1.5;
    ctx.stroke();

    ctx.beginPath();
    ctx.moveTo(centerX, 0);
    ctx.lineTo(centerX - arrowLength, 2*arrowLength);
    ctx.lineTo(centerX + arrowLength, 2*arrowLength);
    ctx.fillStyle = "#1A202C";
    ctx.fill();
}

function drawFigureLines(ctx, centerX, centerY, r, startAngle, endAngle) {

    ctx.lineWidth = 2.5;
    ctx.strokeStyle = "#1E90FF";

    ctx.beginPath();
    ctx.moveTo(centerX + r/2, centerY);
    ctx.lineTo(centerX, centerY + r);
    ctx.lineTo(centerX, centerY);
    ctx.stroke()

    ctx.beginPath();
    ctx.moveTo(centerX, centerY);
    ctx.lineTo(centerX - r, centerY);
    ctx.lineTo(centerX -r, centerY - r/2);
    ctx.lineTo(centerX, centerY - r/2);
    ctx.stroke()

    ctx.beginPath();
    ctx.moveTo(centerX, centerY);
    ctx.arc(centerX, centerY, r, startAngle, endAngle, false);
    ctx.lineTo(centerX, centerY);
    ctx.stroke();
}

function drawStripes(ctx, centerX, centerY, arrowLength) {
    for (let i = 0; i <= 11; i++) {
        ctx.beginPath();
        ctx.moveTo(30 + 40 * i, centerY);
        ctx.lineTo(30 + 40 * i, centerY - arrowLength);
        ctx.lineTo(30 + 40 * i, centerY + arrowLength);

        ctx.moveTo(centerX, 30 + 40 * i);
        ctx.lineTo(centerX - arrowLength, 30 + 40 * i);
        ctx.lineTo(centerX + arrowLength, 30 + 40 * i);

        ctx.strokeStyle = '#1A202C';
        ctx.lineWidth = 1.5;
        ctx.stroke();
        ctx.closePath();
    }
}

function drawCenterDot(ctx, centerX, centerY) {
    //center dot
    ctx.beginPath();
    ctx.arc(centerX, centerY, 2.5, 0, Math.PI * 2);
    ctx.fillStyle = "#1A202C";
    ctx.fill();
}

function drawText(ctx, centerX, centerY, r, width) {
    //X
    ctx.font = "15px Trebuchet MS";
    ctx.fillStyle = "#1A202C";
    ctx.fillText("X", width - 10, centerY + 20);

    //Y
    ctx.font = "15px Trebuchet MS";
    ctx.fillStyle = "#1A202C";
    ctx.fillText("Y", centerX + 10, 13);

    //R
    ctx.font = "15px Trebuchet MS";
    ctx.fillStyle = "#1A202C";
    ctx.textAlign = "center";
    ctx.textBaseline = "middle";
    ctx.fillText("R", centerX + r, centerY + 13);
    ctx.fillText("R", centerX - r, centerY + 13);
    ctx.fillText("R", centerX - 13, centerY - r);
    ctx.fillText("R", centerX - 13, centerY + r);
}

function drawDot(ctx, X, Y) {
    if (Y !== null && X != null) {
        ctx.beginPath();
        ctx.arc(X, Y, 2, 0, Math.PI * 2);
        ctx.fillStyle = "#FF0000";
        ctx.fill();
    }
}

function twoDigitsAfterDot(str) {
    return /^-?\d+(\.\d{1,2})?$/.test(str)
}

function correctNumber (float) {
    return float < 3 && float > -3;
}

