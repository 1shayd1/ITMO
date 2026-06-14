package org.Gh0st1yAnge1.utils;

public class Validator {

    public static boolean validateRouteName(String name){
        return !name.isEmpty();
    }

    public static boolean validateCoordinateX(Float x){
        return x > -720f;
    }

    public static boolean validateCoordinateY(float y){
        return y <= 650;
    }

    public static boolean validateLocationZ(Integer z){
        return z != null;
    }

    public static boolean validateRouteDistance(float distance){
        return distance > 1f;
    }

    public static boolean validateLocationType(Integer type){return type == 1 || type == 2;}

}
