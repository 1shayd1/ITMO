const FILL = "#00FFFF";
const STROKE = "#1E90FF";
const AXIS = "#1A202C";

export function drawPicture(width, height, centerX, centerY, arrowLength, ctx, r, X, Y) {
    ctx.clearRect(0, 0, width, height);

    drawFigures(ctx, centerX, centerY, r);
    drawAxes(ctx, width, height, arrowLength, centerX, centerY);
    drawStripes(ctx, width, height, centerX, centerY, arrowLength);
    drawCenterDot(ctx, centerX, centerY);
    drawText(ctx, centerX, centerY, r, width);
    drawDot(ctx, X, Y);
}

function figuresPath(ctx, centerX, centerY, r) {
    ctx.beginPath();
    ctx.rect(centerX - r / 2, centerY - r, r / 2, r);

    // triangle
    ctx.moveTo(centerX, centerY);
    ctx.lineTo(centerX - r / 2, centerY);
    ctx.lineTo(centerX, centerY + r / 2);
    ctx.closePath();

    // circle quater
    ctx.moveTo(centerX, centerY);
    ctx.arc(centerX, centerY, r, 0, Math.PI / 2, false);
    ctx.closePath();
}

function drawFigures(ctx, centerX, centerY, r) {
    figuresPath(ctx, centerX, centerY, r);
    ctx.fillStyle = FILL;
    ctx.fill();

    figuresPath(ctx, centerX, centerY, r);
    ctx.lineWidth = 2.5;
    ctx.strokeStyle = STROKE;
    ctx.stroke();
}

function drawAxes(ctx, width, height, arrowLength, centerX, centerY) {
    ctx.strokeStyle = AXIS;
    ctx.fillStyle = AXIS;
    ctx.lineWidth = 1.5;

    // Ox
    ctx.beginPath();
    ctx.moveTo(0, centerY);
    ctx.lineTo(width, centerY);
    ctx.stroke();

    ctx.beginPath();
    ctx.moveTo(width, centerY);
    ctx.lineTo(width - 2 * arrowLength, centerY - arrowLength);
    ctx.lineTo(width - 2 * arrowLength, centerY + arrowLength);
    ctx.fill();

    // Oy
    ctx.beginPath();
    ctx.moveTo(centerX, height);
    ctx.lineTo(centerX, 0);
    ctx.stroke();

    ctx.beginPath();
    ctx.moveTo(centerX, 0);
    ctx.lineTo(centerX - arrowLength, 2 * arrowLength);
    ctx.lineTo(centerX + arrowLength, 2 * arrowLength);
    ctx.fill();
}

function drawStripes(ctx, width, height, centerX, centerY, arrowLength) {
    ctx.strokeStyle = AXIS;
    ctx.lineWidth = 1.5;
    ctx.beginPath();

    for (let x = centerX % 40; x < width - 2 * arrowLength; x += 40) {
        ctx.moveTo(x, centerY - arrowLength);
        ctx.lineTo(x, centerY + arrowLength);
    }
    for (let y = centerY % 40 + 40; y < height; y += 40) {
        ctx.moveTo(centerX - arrowLength, y);
        ctx.lineTo(centerX + arrowLength, y);
    }
    ctx.stroke();
}

function drawCenterDot(ctx, centerX, centerY) {
    ctx.beginPath();
    ctx.arc(centerX, centerY, 2.5, 0, Math.PI * 2);
    ctx.fillStyle = AXIS;
    ctx.fill();
}

function drawText(ctx, centerX, centerY, r, width) {
    ctx.fillStyle = AXIS;
    ctx.font = "15px Trebuchet MS";

    ctx.textAlign = "left";
    ctx.textBaseline = "alphabetic";
    ctx.fillText("X", width - 12, centerY + 22);
    ctx.fillText("Y", centerX + 10, 14);

    ctx.font = "13px Trebuchet MS";
    ctx.textAlign = "center";
    ctx.textBaseline = "top";
    ctx.fillText("-R",   centerX - r,     centerY + 8);
    ctx.fillText("-R/2", centerX - r / 2, centerY + 8);
    ctx.fillText("R/2",  centerX + r / 2, centerY + 8);
    ctx.fillText("R",    centerX + r,     centerY + 8);

    ctx.textAlign = "left";
    ctx.textBaseline = "middle";
    ctx.fillText("R",    centerX + 8, centerY - r);
    ctx.fillText("R/2",  centerX + 8, centerY - r / 2);
    ctx.fillText("-R/2", centerX + 8, centerY + r / 2);
    ctx.fillText("-R",   centerX + 8, centerY + r);
}

function drawDot(ctx, X, Y) {
    if (X !== null && Y !== null) {
        ctx.beginPath();
        ctx.arc(X, Y, 2, 0, Math.PI * 2);
        ctx.fillStyle = "#FF0000";
        ctx.fill();
    }
}