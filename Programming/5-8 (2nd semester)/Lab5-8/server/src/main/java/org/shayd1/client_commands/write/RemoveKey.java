package org.shayd1.client_commands.write;

import org.shayd1.client_commands.ClientCommand;
import org.shayd1.db.RouteRepository;
import org.shayd1.manager.CollectionManager;
import org.shayd1.model.Route;
import org.shayd1.request_and_response.Response;

import java.sql.SQLException;

public class RemoveKey implements ClientCommand {

    private final CollectionManager collectionManager;
    private final RouteRepository routeRepository;

    public RemoveKey(CollectionManager collectionManager, RouteRepository routeRepository){
        this.collectionManager = collectionManager;
        this.routeRepository = routeRepository;
    }

    @Override
    public Response execute(String arg, Route route, Long userid) {

        if (arg == null || arg.isBlank()){
            return new Response(false, "Usage: remove_key <key>");
        }

        int key;
        try{
            key = Integer.parseInt(arg);
        } catch (NumberFormatException ex){
            return new Response(false, "Key must be integer.");
        }

        try {
            boolean isRemoved = routeRepository.removeByKey(key, userid);
            if (isRemoved) {
                collectionManager.removeByKey(key, userid);
                return new Response(true, "Successfully removed!");
            }
            return new Response(false, "Key does not exists");
        } catch (SQLException e) {
            return new Response(false, "Database error: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "remove_key";
    }

    @Override
    public String getDescription() {
        return "removes element using a key";
    }
}
