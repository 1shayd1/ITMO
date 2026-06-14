package org.Gh0st1yAnge1.server_commands;

import org.Gh0st1yAnge1.manager.CollectionManager;
import org.Gh0st1yAnge1.manager.FileManager;
import org.Gh0st1yAnge1.model.Route;

import java.util.LinkedHashMap;

public class SaveWithPath implements ServerCommand{

    private final CollectionManager collectionManager;

    public SaveWithPath (CollectionManager collectionManager){
        this.collectionManager = collectionManager;
    }

    @Override
    public String getName() {
        return "save_with_path";
    }

    @Override
    public String getDescription() {
        return "saves collection using inputted path";
    }

    @Override
    public String execute(String arg) {
        String path = (arg != null) ? arg : "backup.json";
        FileManager backupManager = new FileManager(path);
        LinkedHashMap<Integer, Route> snapshot = new LinkedHashMap<>(collectionManager.getMap());
        return backupManager.saveCollection(snapshot);
    }
}
