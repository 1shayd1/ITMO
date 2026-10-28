import {drawPicture} from "./drawer.js";

window.onload = function () {
    drawPage()
}

function drawPage() {
    const canvas = document.getElementById('canvas');
    const ctx = canvas.getContext('2d');
    const button = document.getElementById('clear-button');
    const radios = document.querySelectorAll('input[name="radio"]');
    let currentR = 3 * 40;
    let errorMessage = document.getElementById("y-error");

    const form = document.querySelector('.input-block');

    // canvas settings
    const width = canvas.width;
    const height = canvas.height;
    const centerX = width / 2;
    const centerY = height / 2;
    const arrowLength = 4;
    const startAngle = Math.PI / 180 * 90
    const endAngle = Math.PI;

    async function submitPoint(pointX, pointY) {
        try {
            const body = new URLSearchParams({
                x: pointX,
                y: pointY,
                r: currentR / 40
            });

            const response = await fetch("/fcgi-bin/hello-world.jar", {
                method: "POST",
                body: body
            });

            if (!response.ok) {
                errorMessage.textContent = await response.text();
                return
            }

            const record = await response.json();
            addToTable(record);

            const X = centerX + pointX * 40;
            const Y = centerY - pointY * 40;
            errorMessage.textContent = "";
            drawPicture(width, height, centerX, centerY, arrowLength, ctx, currentR, X, Y, startAngle, endAngle);
        } catch (error) {
            document.getElementById("y-error").textContent = "Could not submit point";
            console.error(error);
        }
    }
    function simpleDraw() {
        drawPicture(width, height, centerX, centerY, arrowLength, ctx, currentR, null, null, startAngle, endAngle);
    }
    simpleDraw();

    loadHistory();

    button.addEventListener('click', async () => {
        if (await clearTable()){
            simpleDraw();
        }
    });

    canvas.addEventListener('click', (e) => {
        const rect = canvas.getBoundingClientRect();
        const px = (e.clientX - rect.left) * (canvas.width / rect.width);
        const py = (e.clientY - rect.top) * (canvas.height / rect.height);

        const x = Math.round((px-centerX) / 40 * 100) / 100
        const y = Math.round((centerY-py) / 40 * 100) / 100

        submitPoint(x, y);
    });

    radios.forEach(radio => {
        radio.addEventListener('change',() => {
            const newR = document.querySelector('input[name="radio"]:checked').value * 40;
            if (newR !== currentR) {
                currentR = newR;
                errorMessage.textContent = "";
                simpleDraw();
            }
        });
    })

    form.addEventListener('submit', (e) => {
        e.preventDefault();
        const checkX = parseFloat(document.getElementById('x-select').value);
        const raw_Y = document.getElementById('y-text').value;
        const checkY_1 = raw_Y.replace(',','.');


        if (!twoDigitsAfterDot(checkY_1)) {
            errorMessage.textContent = 'Y must ∈ (-5;5) and have no more than 2 digits after dot/comma';
            simpleDraw()
            return;
        }

        const checkY = parseFloat(checkY_1);

        if (!correctNumber(checkY)) {
            errorMessage.textContent = 'Y must ∈ (-5;5)';
            simpleDraw()
            return;
        }

        submitPoint(checkX, checkY);
    })
}


function twoDigitsAfterDot(str) {
    return /^-?\d+(\.\d{1,2})?$/.test(str)
}

function correctNumber (float) {
    return float < 3 && float > -5;
}

function addToTable (record) {
    const tableRow = document.querySelector(".table-scroll table tbody");
    const newRow = document.createElement("tr");

    newRow.innerHTML = `
        <td>${record.x.toFixed(2)}</td>
        <td>${record.y.toFixed(2)}</td>
        <td>${record.r.toFixed(1)}</td>
        <td>${record.isHit}</td>
        <td>${new Date(record.currentTime).toLocaleString()}</td>
        <td>${record.workTime}</td>
    `
    tableRow.appendChild(newRow);
}

async function clearTable() {
    try {
        const response = await fetch("/fcgi-bin/hello-world.jar", {
            method: "DELETE",
        })

        if (!response.ok) {
            document.getElementById("y-error").textContent = await response.text();
            return false;
        }

        document.querySelector(".table-scroll table tbody").innerHTML = "";
        return true;
    } catch (error) {
        console.error(error);
        document.getElementById("y-error").textContent = "Could not clear table";
        return false;
    }

}

async function loadHistory () {
    try {
        const response = await fetch("/fcgi-bin/hello-world.jar");
        if (!response.ok) {
            document.getElementById("y-error").textContent = await response.text();
            return;
        }

        const records = await response.json();

        records.forEach((record) => {
            addToTable(record);
        })
    } catch (error) {
        document.getElementById("y-error").textContent = "Could not load history";
        console.error(error);
    }
}