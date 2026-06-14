package org.Gh0st1yAnge1.gui.visualization;

import org.Gh0st1yAnge1.gui.models.RouteTableItem;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CanvasRenderer {

    private static final double MIN_RADIUS = 8;
    private static final double MAX_RADIUS = 40;
    private static final double PULSE_AMP  = 3.0;
    private static final double PULSE_FREQ = 2.0;

    private final Canvas        canvas;
    private final ColorManager colorManager = new ColorManager();
    private final AnimationTimer timer;

    private List<RouteTableItem> items = new ArrayList<>();
    private Consumer<RouteTableItem> onClickHandler;
    private long myOwnerId = -1;

    private record CircleInfo(RouteTableItem item, double cx, double cy, double baseR) {}
    private List<CircleInfo> circles = new ArrayList<>();

    public CanvasRenderer(Canvas canvas) {
        this.canvas = canvas;

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                drawFrame(now);
            }
        };
        timer.start();

        canvas.setOnMouseClicked(e -> handleClick(e.getX(), e.getY()));
    }

    public void setItems(List<RouteTableItem> items) {
        this.items = new ArrayList<>(items);
    }

    public void setMyOwnerId(long id) { this.myOwnerId = id; }

    public void setOnClick(Consumer<RouteTableItem> handler) {
        this.onClickHandler = handler;
    }

    public void stop() { timer.stop(); }

    private void drawFrame(long nowNanos) {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.web("#1a1a2e"));
        gc.fillRect(0, 0, w, h);

        gc.setStroke(Color.web("#ffffff10"));
        gc.setLineWidth(0.5);
        for (double x = 0; x < w; x += 40) gc.strokeLine(x, 0, x, h);
        for (double y = 0; y < h; y += 40) gc.strokeLine(0, y, w, y);

        if (items.isEmpty()) {
            gc.setFill(Color.web("#ffffff44"));
            gc.setFont(Font.font(14));
            gc.fillText("No routes to display", w / 2 - 70, h / 2);
            return;
        }

        double minX = items.stream().mapToDouble(RouteTableItem::getCoordX).min().orElse(0);
        double maxX = items.stream().mapToDouble(RouteTableItem::getCoordX).max().orElse(1);
        double minY = items.stream().mapToDouble(RouteTableItem::getCoordY).min().orElse(0);
        double maxY = items.stream().mapToDouble(RouteTableItem::getCoordY).max().orElse(1);
        double minD = items.stream().mapToDouble(RouteTableItem::getDistance).min().orElse(0);
        double maxD = items.stream().mapToDouble(RouteTableItem::getDistance).max().orElse(1);

        double rangeX = (maxX - minX) == 0 ? 1 : maxX - minX;
        double rangeY = (maxY - minY) == 0 ? 1 : maxY - minY;
        double rangeD = (maxD - minD) == 0 ? 1 : maxD - minD;

        double padding = 50;
        double t = nowNanos / 1_000_000_000.0;

        List<CircleInfo> newCircles = new ArrayList<>();

        for (RouteTableItem item : items) {
            double cx = padding + (item.getCoordX() - minX) / rangeX * (w - 2 * padding);
            double cy = padding + (item.getCoordY() - minY) / rangeY * (h - 2 * padding);

            double norm = (item.getDistance() - minD) / rangeD;
            double baseR = MIN_RADIUS + norm * (MAX_RADIUS - MIN_RADIUS);

            double phase = item.getKey() * 0.7;
            double pulse = Math.sin(2 * Math.PI * PULSE_FREQ * t + phase) * PULSE_AMP;
            double r = baseR + pulse;

            Color baseColor = colorManager.colorFor(item.getOwnerId());
            boolean isMine  = item.getOwnerId() == myOwnerId;

            gc.setFill(baseColor.deriveColor(0, 1, 0.6, 0.3));
            gc.fillOval(cx - r * 1.5, cy - r * 1.5, r * 3, r * 3);

            gc.setFill(baseColor.deriveColor(0, 1, 1, 0.85));
            gc.fillOval(cx - r, cy - r, r * 2, r * 2);

            gc.setStroke(isMine ? Color.WHITE : baseColor.brighter());
            gc.setLineWidth(isMine ? 2.0 : 1.0);
            gc.strokeOval(cx - r, cy - r, r * 2, r * 2);

            gc.setFill(Color.WHITE);
            gc.setFont(Font.font(10));
            gc.fillText(String.valueOf(item.getKey()), cx - 4, cy + 4);

            newCircles.add(new CircleInfo(item, cx, cy, baseR));
        }

        circles = newCircles;
    }

    private void handleClick(double mx, double my) {
        if (onClickHandler == null) return;
        for (CircleInfo ci : circles) {
            double dist = Math.hypot(mx - ci.cx(), my - ci.cy());
            if (dist <= ci.baseR() + PULSE_AMP + 2) {
                onClickHandler.accept(ci.item());
                return;
            }
        }
    }
}