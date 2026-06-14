package org.Gh0st1yAnge1.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.utils.IdGenerator;
import org.Gh0st1yAnge1.utils.LocalDateAdapter;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;


public class FileManager {
    private final String filePath;

    public FileManager(String filePath){
        this.filePath = filePath;
    }

    public LinkedHashMap<Integer, Route> loadCollection(){

        LinkedHashMap<Integer, Route> map = new LinkedHashMap<>();

        if (filePath == null){
            System.out.println("File path is not found.");
            return map;
        }

        File file = new File(filePath);
        if (!file.exists()){
            System.out.println("File doesn't exists.");
            return map;
        }

        if (!file.canRead()){
            System.out.println("You don't have permission to read this file.");
            return map;
        }

        try (FileReader reader = new FileReader(file)){

            Gson gson =new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter()).setPrettyPrinting().create();
            Type type = new TypeToken<LinkedHashMap<Integer, Route>>(){}.getType();
            map = gson.fromJson(reader, type);
            if (map == null){
                map = new LinkedHashMap<>();
            }

        } catch (IOException e) {
            System.out.println("Error reading file.");
        }
        return map;
    }

    public String saveCollection(LinkedHashMap<Integer, Route> collection){
        if (filePath == null){
            return ("File path is not found.");
        }

        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()){
            if (!parentDir.mkdirs()){
                return "Failed to create directories: " + filePath;
            }
        }

        try(BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(filePath))){

            Gson gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter()).setPrettyPrinting().create();
            String json = gson.toJson(collection);

            bos.write(json.getBytes(StandardCharsets.UTF_8));
            bos.flush();

        } catch (IOException e){
            return "Error writing file.";
        }

        return "Collection successfully saved!";
    }

}