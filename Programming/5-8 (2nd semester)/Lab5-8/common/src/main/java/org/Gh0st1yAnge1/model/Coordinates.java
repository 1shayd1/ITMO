package org.Gh0st1yAnge1.model;

import java.io.Serial;
import java.io.Serializable;

public class Coordinates implements Comparable<Coordinates>, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Float x;
    private float y;

    public Coordinates(Float x, float y){
        this.x = x;
        this.y = y;
    }

    @Override
    public int compareTo(Coordinates other) {
        return this.x.compareTo(other.getX());
    }

    public void setX(Float x) {
            this.x = x;
    }
    public Float getX() {
        return x;
    }

    public void setY(float y) {
            this.y = y;
    }
    public float getY() {
        return y;
    }

    @Override
    public String toString() {
        return "Coordinates{\n" +
                "        x=" + x +
                ",\n        y=" + y + '\n' +
                "    }";
    }

}
