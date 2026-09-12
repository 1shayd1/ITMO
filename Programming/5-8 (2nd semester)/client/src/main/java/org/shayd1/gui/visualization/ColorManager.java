package org.shayd1.gui.visualization;

import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

public class ColorManager {

    private static final Color[] PALETTE = {
            Color.web("#4FC3F7"),   // sky blue
            Color.web("#81C784"),   // green
            Color.web("#FFB74D"),   // amber
            Color.web("#F06292"),   // pink
            Color.web("#CE93D8"),   // purple
            Color.web("#4DB6AC"),   // teal
            Color.web("#FF8A65"),   // deep orange
            Color.web("#90A4AE"),   // blue grey
    };

    private final Map<Long, Color> cache = new HashMap<>();
    private int nextIndex = 0;

    public Color colorFor(long ownerId) {
        return cache.computeIfAbsent(ownerId, id -> {
            Color c = PALETTE[nextIndex % PALETTE.length];
            nextIndex++;
            return c;
        });
    }
}