package org.Gh0st1yAnge1.client_commands.read;

import org.Gh0st1yAnge1.client_commands.ClientCommand;
import org.Gh0st1yAnge1.manager.CollectionManager;
import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.Response;

public class AverageOfDistance implements ClientCommand {

    private final CollectionManager collectionManager;

    public AverageOfDistance(CollectionManager collectionManager){
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(String arg, Route route, Long userId) {

        if (arg != null){
            return new Response(false, "Usage: average_of_distance");
        }

        double avgDist = collectionManager.averageOfDistance();

        return new Response(true, "" + avgDist);
    }

    public String getName() {
        return "average_of_distance";
    }

    @Override
    public String getDescription() {
        return "gives you average of distance fields in collection";
    }
}
