window.onload = function () {
    const canvas = document.getElementById('canvas');
    const ctx = canvas.getContext('2d');

    const ValueR = 3 * 40 ;

    const width = canvas.width;
    const height = canvas.height;
    const centerX = width / 2;
    const centerY = height / 2;
    const arrowLength = 4;

    drawPicture(width, height, centerX, centerY, arrowLength, ctx, ValueR);
}

function drawPicture(width, height, centerX, centerY, arrowLength, ctx, r) {

    //circle-quater
    const startAngle = Math.PI / 180 * 270
    const endAngle = 0;

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

    ctx.lineWidth = 2.5;
    ctx.strokeStyle = "#1E90FF";

    //figure lines
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


    //stripes
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

    //center dot
    ctx.beginPath();
    ctx.arc(centerX, centerY, 2.5, 0, Math.PI * 2);
    ctx.fillStyle = "#1A202C";
    ctx.fill();

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
    ctx.fillText("R/2", centerX + r/2, centerY - 13);
    ctx.fillText("R/2", centerX + 13, centerY - r/2);
    ctx.fillText("R", centerX - 13, centerY - r);
    ctx.fillText("R", centerX - 13, centerY + r);
}
