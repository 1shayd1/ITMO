package org.Gh0st1yAnge1.client_commands.write;

import org.Gh0st1yAnge1.client_commands.ClientCommand;
import org.Gh0st1yAnge1.db.RouteRepository;
import org.Gh0st1yAnge1.manager.CollectionManager;
import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.Response;

import java.sql.SQLException;

public class RemoveGreaterKey implements ClientCommand {

    private final CollectionManager collectionManager;
    private final RouteRepository routeRepository;

    public RemoveGreaterKey(CollectionManager collectionManager, RouteRepository routeRepository){
        this.collectionManager = collectionManager;
        this.routeRepository = routeRepository;
    }

    @Override
    public Response execute(String arg, Route route, Long userId) {

        if (arg == null || arg.isBlank()){
            return new Response(false, "Usage: remove_greater_key <key>");
        }

        int key;
        try{
            key = Integer.parseInt(arg);
        } catch (NumberFormatException ex){
            return new Response(false, "Key must be integer.");
        }

        try {
            int removed = routeRepository.removeGreaterKey(key, userId);
            if (removed > 0){
                collectionManager.removeGreaterKey(key, userId);
                return new Response(true, "Successfully removed!" + "Number of removed elements: " + removed);
            }
            return new Response(true, "Collection size didn't change.");
        } catch (SQLException e) {
            return new Response(false, "Database error: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "remove_greater_key";
    }

    @Override
    public String getDescription() {
        return "removes all collection elements,\nwhich key is more than inserted value";
    }
}
