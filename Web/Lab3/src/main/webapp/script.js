import {drawPicture} from "./drawer.js";

const SCALE = 40;

window.onload = () => {
    const canvas = document.getElementById('canvas');
    const ctx = canvas.getContext('2d');
    const form = document.querySelector('.input-block');
    const xInput = document.getElementById('x-input');
    const yText = document.getElementById('y-text');
    const rSelect = document.getElementById('r-select');
    const error = document.getElementById('error');
    const centerX = canvas.width / 2;
    const centerY = canvas.height / 2;

    const rows = document.querySelectorAll('tbody tr');
    const last = rows.length > 0 ? rows[rows.length - 1] : null;

    function redraw() {
        const r = rSelect.value ? Number(rSelect.value) : 3;
        const X = last ? centerX + parseFloat(last.dataset.x) * SCALE : null;
        const Y = last ? centerY - parseFloat(last.dataset.y) * SCALE : null;
        drawPicture(canvas.width, canvas.height, centerX, centerY, 4, ctx,
            r * SCALE, X, Y);
    }
    redraw();
    rSelect.addEventListener('change', redraw);

    document.querySelectorAll('.x-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            xInput.value = btn.value;
            document.querySelectorAll('.x-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            error.textContent = '';
        });
    });

    form.addEventListener('submit', (e) => {
        const yRaw = yText.value.trim().replace(',', '.');
        const y = parseFloat(yRaw);
        let msg = '';

        if (xInput.value === '') msg = 'Choose X';
        else if (!/^-?\d+(\.\d{1,2})?$/.test(yRaw)) msg = 'Y must be a number (max 2 digits after dot)';
        else if (y <= -5 || y >= 5) msg = 'Y must be in (-5; 5)';
        else if (rSelect.value === '') msg = 'Choose R';

        if (msg) {
            e.preventDefault();
            error.textContent = msg;
        } else {
            yText.value = yRaw;
        }
    });

    canvas.addEventListener('click', (e) => {
        if (rSelect.value === '') {
            error.textContent = 'Choose R before clicking on the graph';
            return;
        }
        const rect = canvas.getBoundingClientRect();
        const px = (e.clientX - rect.left) * (canvas.width / rect.width);
        const py = (e.clientY - rect.top) * (canvas.height / rect.height);
        const x = ((px - centerX) / SCALE).toFixed(2);
        const y = ((centerY - py) / SCALE).toFixed(2);

        const params = new URLSearchParams({x, y, r: rSelect.value});
        window.location.href = 'controller?' + params;
    });
};