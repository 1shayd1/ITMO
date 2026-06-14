package org.Gh0st1yAnge1.client_commands.write;

import org.Gh0st1yAnge1.client_commands.ClientCommand;
import org.Gh0st1yAnge1.db.RouteRepository;
import org.Gh0st1yAnge1.manager.CollectionManager;
import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.Response;

import java.sql.SQLException;

public class RemoveGreater implements ClientCommand {

    private final CollectionManager collectionManager;
    private final RouteRepository routeRepository;

    public RemoveGreater(CollectionManager collectionManager, RouteRepository routeRepository){
        this.collectionManager = collectionManager;
        this.routeRepository = routeRepository;
    }

    @Override
    public Response execute(String arg, Route route, Long userId) {

        if (arg != null){
            return new Response(false, "Usage: remove_greater");
        }

        if (route == null){
            return new Response(false, "Route must not be null");
        }

        try {
            int removedRoutes = routeRepository.removeGreater(route, userId);
            if (removedRoutes > 0){
                collectionManager.removeGreater(route, userId);
                return  new Response(true, "Successfully removed!" + "Number of removed elements: " + removedRoutes);
            }
            return new Response(true, "Collection size didn't change.");
        } catch (SQLException e) {
            return new Response(false, "Database error: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "remove_greater";
    }

    @Override
    public String getDescription() {
        return "removes all collection elements,\nwho is more than inserted";
    }
}
