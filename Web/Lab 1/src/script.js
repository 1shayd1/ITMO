window.onload = function () {
    const canvas = document.getElementById('canvas');
    const ctx = canvas.getContext('2d');

    const ValueR = 2;

    const width = canvas.width;
    const height = canvas.height;
    const centerX = width / 2;
    const centerY = height / 2;
    const arrowLength = 4;

    drawPicture(width, height, centerX, centerY, arrowLength, ctx);
}

function drawPicture(width, height, centerX, centerY, arrowLength, ctx, r) {

    //center dot
    ctx.arc(centerX, centerY, 2.5, 0, Math.PI * 2);
    ctx.fillStyle = 'black';
    ctx.fill();

    //Ox
    ctx.beginPath();
    ctx.moveTo(0, centerY);
    ctx.lineTo(width, centerY);

    ctx.lineTo(width - arrowLength, centerY - arrowLength);
    ctx.moveTo(width, centerY);
    ctx.lineTo(width - arrowLength, centerY + arrowLength);

    //Oy
    ctx.moveTo(centerX, height);
    ctx.lineTo(centerX, 0);

    ctx.lineTo(centerX - arrowLength, arrowLength);
    ctx.moveTo(centerX, 0);
    ctx.lineTo(centerX + arrowLength, arrowLength);

    //stripes
    for (let i = 0; i <= 11; i++) {
        ctx.moveTo(30 + 40 * i, centerY);
        ctx.lineTo(30 + 40 * i, centerY - arrowLength);
        ctx.lineTo(30 + 40 * i, centerY + arrowLength);

        ctx.moveTo(centerX, 30 + 40 * i);
        ctx.lineTo(centerX - arrowLength, 30 + 40 * i);
        ctx.lineTo(centerX + arrowLength, 30 + 40 * i);
    }

    ctx.strokeStyle = 'black';
    ctx.stroke();
}
