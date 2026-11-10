package org.shaydi;

import java.io.Serializable;

public class Result implements Serializable {

    private double x;
    private double y;
    private double r;
    private boolean hit;
    private String time;
    private long workTime;

    public Result(){}

    public Result(double x, double y, double r, boolean hit, String time, long workTime) {
        this.x = x;
        this.y = y;
        this.r = r;
        this.hit = hit;
        this.time = time;
        this.workTime = workTime;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getR() {
        return r;
    }

    public boolean getHit() {
        return hit;
    }

    public String getTime() {
        return time;
    }

    public long getWorkTime() {
        return workTime;
    }
}
