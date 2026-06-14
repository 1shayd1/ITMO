package org.Gh0st1yAnge1.client_commands.read;

import org.Gh0st1yAnge1.client_commands.ClientCommand;
import org.Gh0st1yAnge1.db.RouteRepository;
import org.Gh0st1yAnge1.manager.CollectionManager;
import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.Response;

public class CheckInsertKey implements ClientCommand {
    private final CollectionManager collectionManager;
    private final RouteRepository routeRepository;

    public CheckInsertKey(CollectionManager collectionManager, RouteRepository routeRepository){
        this.collectionManager = collectionManager;
        this.routeRepository = routeRepository;
    }

    @Override
    public Response execute(String arg, Route route, Long userId) {

        if (arg == null || arg.isBlank()){
            return new Response(false, "Usage: insert <key>");
        }

        int key;
        try {
            key = Integer.parseInt(arg);
        } catch (NumberFormatException ex){
            return new Response(false, "Key must be integer.");
        }

        if (collectionManager.checkKey(key)){
            return new Response(false, "Key already exists.");
        } else {
            return new Response(true, "Key is available!");
        }
    }

    @Override
    public String getName() {
        return "check_key";
    }

    @Override
    public String getDescription() {
        return "checks_key";
    }
}
