package org.shayd1.model;

import java.io.Serial;
import java.io.Serializable;

public class Location implements Comparable<Location>, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private double x;
    private double y;
    private Integer z;
    private String name;

    //without name
    public Location(double x, double y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = null;
    }

    //with name
    public Location(double x, double y, int z, String name){
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = name;
    }

    @Override
    public int compareTo(Location other) {
        if (this.name == null && other.getName() == null) return 0;
        if (this.name == null) return 1;
        if (other.getName() == null) return -1;
        return this.name.compareTo(other.getName());
    }

    public void setX(Double x) {
        this.x = x;
    }
    public double getX() {
        return x;
    }

    public void setY(Double intY) { this.y = y;}
    public double getIntY() {
        return y;
    }

    public void setZ(Integer intZ) {
        this.z = z;
    }
    public Integer getIntZ() {
        return z;
    }

    public void setName(String name) {
            this.name = name;
    }
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Location{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", name='" + name + '\'' +
                '}';
    }
}
