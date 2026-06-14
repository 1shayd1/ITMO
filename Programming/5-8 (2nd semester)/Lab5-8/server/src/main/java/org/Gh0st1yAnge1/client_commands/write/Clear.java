package org.Gh0st1yAnge1.client_commands.write;

import org.Gh0st1yAnge1.client_commands.ClientCommand;
import org.Gh0st1yAnge1.db.RouteRepository;
import org.Gh0st1yAnge1.manager.CollectionManager;
import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.Response;

import java.sql.SQLException;

public class Clear implements ClientCommand {

    private final CollectionManager collectionManager;
    private final RouteRepository routeRepository;

    public Clear(CollectionManager collectionManager, RouteRepository routeRepository){
        this.collectionManager = collectionManager;
        this.routeRepository = routeRepository;
    }

    @Override
    public Response execute(String arg, Route route, Long userId) {

        if (arg != null){
            return new Response(false, "Usage: clear");
        }

        try {
            routeRepository.clear(userId);
            collectionManager.clearOnly(userId);
            return new Response(true, "Successfully cleared!");
        } catch (SQLException e) {
            return new Response(false, "Database error: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "clear";
    }

    @Override
    public String getDescription() {
        return "clears collection";
    }
}
