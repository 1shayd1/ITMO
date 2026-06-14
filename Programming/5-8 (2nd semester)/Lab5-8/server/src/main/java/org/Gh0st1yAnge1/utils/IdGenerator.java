package org.Gh0st1yAnge1.utils;

public class IdGenerator {

    private static int currentMaxId = 0;

    public static int generate(){
        currentMaxId++;
        return currentMaxId;
    }

    public static void compareMaxId(int id){
        if (id > currentMaxId){
            currentMaxId = id;
        }
    }

    public static void reset(){
        currentMaxId = 0;
    }
}
