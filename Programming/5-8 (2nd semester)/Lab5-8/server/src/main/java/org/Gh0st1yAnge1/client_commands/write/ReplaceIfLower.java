package org.Gh0st1yAnge1.client_commands.write;

import org.Gh0st1yAnge1.client_commands.ClientCommand;
import org.Gh0st1yAnge1.db.RouteRepository;
import org.Gh0st1yAnge1.manager.CollectionManager;
import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.Response;

import java.sql.SQLException;

public class ReplaceIfLower implements ClientCommand {

    private final CollectionManager collectionManager;
    private final RouteRepository routeRepository;

    public ReplaceIfLower(CollectionManager collectionManager, RouteRepository routeRepository){
        this.collectionManager = collectionManager;
        this.routeRepository = routeRepository;
    }

    @Override
    public Response execute(String arg, Route route, Long userId) {

        if (arg == null || arg .isBlank()) {
            return new Response(false, "Usage: replace_if_lower <key>");
        }

        if (route == null){
            return new Response(false, "Route must not be null");
        }

        int key;
        try{
            key = Integer.parseInt(arg);
        }catch (NumberFormatException ex){
           return new Response(false, "Key must be integer.");
        }

        try {
            boolean isReplaced = routeRepository.replaceIfLower(key, route, userId);
            if (isReplaced) {
                collectionManager.replaceIfLower(key, route, userId);
                return  new Response(true, "Element replaced!");
            }
            return  new Response(true, "New element is more than old or they're equals.");
        } catch (SQLException e) {
            return new Response(false, "Database error: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "replace_if_lower";
    }

    @Override
    public String getDescription() {
        return "replaces element using a key,\nif new value is less than old";
    }
}
