package org.shayd1.client_commands.read;

import org.shayd1.client_commands.ClientCommand;
import org.shayd1.db.RouteRepository;
import org.shayd1.manager.CollectionManager;
import org.shayd1.model.Route;
import org.shayd1.request_and_response.Response;

import java.sql.SQLException;

public class CheckUpdateKey implements ClientCommand {
    private final CollectionManager collectionManager;
    private final RouteRepository routeRepository;

    public CheckUpdateKey(CollectionManager collectionManager, RouteRepository routeRepository){
        this.collectionManager = collectionManager;
        this.routeRepository = routeRepository;
    }

    @Override
    public Response execute(String arg, Route route, Long userId) {

        if (arg == null || arg.isBlank()) {
            return new Response(false, "Usage: insert <key>");
        }

        int key;
        try {
            key = Integer.parseInt(arg);
        } catch (NumberFormatException ex) {
            return new Response(false, "Key must be integer.");
        }

        if (!collectionManager.checkKey(key)) {
            return new Response(false, "Key doesn't exist.");
        }

        try {
            if (!routeRepository.checkOwnership(key, userId)){
                return new Response(false, "You can only modify your own routes.");
            }
        } catch (SQLException e) {
            return new Response(false, "DB error: " + e.getMessage());
        }
        return new Response(true, "Key exists and belongs to you. Enter new data:");
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
