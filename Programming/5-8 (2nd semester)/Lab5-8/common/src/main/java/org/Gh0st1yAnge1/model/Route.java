package org.Gh0st1yAnge1.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

public class Route implements Comparable<Route>, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long key;
    private String name;
    private Coordinates coordinates;
    private java.time.LocalDate creationDate;
    private Location from;
    private Location to;
    private float distance;
    private Long ownerId;

    public Route() {}

    public Route(String name, Coordinates coordinates, Location from, Location to, float distance){
        this.key = null;
        this.ownerId = null;
        this.name = name;
        this.coordinates = coordinates;
        this.to = to;
        this.from = from;
        this.distance = distance;
        this.creationDate = LocalDate.now();
    }

    @Override
    public int compareTo(Route other) {
        if (other == null) return 1;
        if (this.coordinates == null && other.getCoordinates() == null) return 0;
        if (this.coordinates == null) return -1;
        if (other.getCoordinates() == null) return 1;
        return this.coordinates.compareTo(other.getCoordinates());
    }

    public void setKey(Long id) {this.key = id; }
    public Long getKey() {
        return key;
    }

    public void setOwnerId(Long id) {this.ownerId = id; }
    public Long getOwnerId() { return  ownerId; }

    public void setName(String name) {
            this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setCoordinates(Coordinates coordinates) {
            this.coordinates = coordinates;
    }
    public Coordinates getCoordinates() {
        return coordinates;
    }

    public LocalDate getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDate creationDate) { this.creationDate = creationDate; }

    public void setFrom(Location from) {
            this.from = from;
    }
    public Location getFrom() {
        return from;
    }

    public void setTo(Location to) {
            this.to = to;
    }
    public Location getTo() {
        return to;
    }

    public void setDistance(float distance) {
            this.distance = distance;
    }
    public float getDistance() {
        return distance;
    }

    @Override
    public String toString() {
        return "Route{" +
                "key=" + key +
                ", name='" + name + '\'' +
                ", coordinates=" + coordinates +
                ", creationDate=" + creationDate +
                ", from=" + from +
                ", to=" + to +
                ", distance=" + distance +
                ", ownerId=" + ownerId +
                '}';
    }
}