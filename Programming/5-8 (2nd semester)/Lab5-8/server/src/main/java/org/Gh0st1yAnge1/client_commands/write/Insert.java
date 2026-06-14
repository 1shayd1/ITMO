package org.Gh0st1yAnge1.client_commands.write;

import org.Gh0st1yAnge1.client_commands.ClientCommand;
import org.Gh0st1yAnge1.db.RouteRepository;
import org.Gh0st1yAnge1.manager.CollectionManager;
import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.Response;
import java.sql.SQLException;

public class Insert implements ClientCommand {
    private final CollectionManager collectionManager;
    private final RouteRepository routeRepository;

    public Insert(CollectionManager collectionManager, RouteRepository routeRepository) {
        this.collectionManager = collectionManager;
        this.routeRepository = routeRepository;
    }

    @Override
    public Response execute(String arg, Route route, Long userId) {
        if (route == null) {
            return new Response(false, "Route must not be null");
        }

        int key;
        try {
            key = Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            return new Response(false, "Key must be Integer.");
        }

        if (userId == null) {
            return new Response(false, "You must be logged in to insert");
        }

        try {
            if (routeRepository.insert(key, route, userId)) {
                collectionManager.insert(key, route, userId);
                return new Response(true, "Element inserted");
            } else {
                return new Response(false, "Failed to insert into database");
            }
        } catch (SQLException e) {
            return new Response(false, "Database error: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "insert";
    }

    @Override
    public String getDescription() {
        return "adds new element using a key";
    }
}