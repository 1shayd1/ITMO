package org.shayd1.manager;

import org.shayd1.model.Route;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CollectionManager {

    private final ConcurrentHashMap<Integer, Route> collection = new ConcurrentHashMap<>();
    private final LocalDate initializationDate = LocalDate.now();

    public void load(Map<Integer, Route> loaded) {
        collection.clear();
        if (loaded != null) collection.putAll(loaded);
    }

    public boolean checkKey(int key) {
        return collection.containsKey(key);
    }

    public void insert(Integer key, Route route, long userId) {
        route.setOwnerId(userId);
        route.setKey((long) key);
        collection.put(key, route);
    }

    public boolean update(Integer key, Route route, long userId) {
        Route existing = collection.get(key);
        if (existing != null && existing.getOwnerId() == userId) {
            route.setOwnerId(userId);
            route.setKey((long) key);
            collection.put(key, route);
            return true;
        }
        return false;
    }

    public boolean removeByKey(Integer key, long userId) {
        Route route = collection.get(key);
        if (route != null && route.getOwnerId() == userId) {
            return collection.remove(key) != null;
        }
        return false;
    }

    public int removeGreaterKey(Integer key, long userId) {
        List<Integer> toRemove = collection.entrySet().stream()
                .filter(entry -> entry.getValue().getOwnerId() == userId)
                .filter(entry -> entry.getKey() > key)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        toRemove.forEach(collection::remove);
        return toRemove.size();
    }

    public int removeGreater(Route ref, long userId) {
        List<Integer> toRemove = collection.entrySet().stream()
                .filter(entry -> entry.getValue().getOwnerId() == userId)
                .filter(entry -> entry.getValue().compareTo(ref) > 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        toRemove.forEach(collection::remove);
        return toRemove.size();
    }

    public boolean replaceIfLower(Integer key, Route newRoute, long userId) {
        boolean[] replaced = {false};
        collection.compute(key, (k, current) -> {
            if (current != null && current.getOwnerId() == userId && current.compareTo(newRoute) > 0) {
                replaced[0] = true;
                newRoute.setOwnerId(userId);
                newRoute.setKey((long) key);
                return newRoute;
            }
            return current;
        });
        return replaced[0];
    }


    public void clearOnly(long userId) {
        collection.entrySet().removeIf(e -> e.getValue().getOwnerId() == userId);
    }

    public Map<Integer, Route> getMap() {
        return Collections.unmodifiableMap(collection);
    }

    public List<Route> showCollection() {
        return collection.values().stream()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
    }
    
    public double averageOfDistance() {
        return collection.values().stream()
                .mapToDouble(Route::getDistance)
                .average()
                .orElse(0.0);
    }

    public long countByDistance(double distance) {
        return collection.values().stream()
                .filter(r -> r.getDistance() == distance)
                .count();
    }

    public List<Route> filterLessThanDistance(double distance) {
        return collection.values().stream()
                .filter(r -> r.getDistance() < distance)
                .collect(Collectors.toList());
    }

    public String info() {
        return "ConcurrentHashMap |" + collection.size() + "|" + initializationDate;
    }
}