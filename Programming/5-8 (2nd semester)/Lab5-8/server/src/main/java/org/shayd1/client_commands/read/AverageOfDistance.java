package org.shayd1.client_commands.read;

import org.shayd1.client_commands.ClientCommand;
import org.shayd1.manager.CollectionManager;
import org.shayd1.model.Route;
import org.shayd1.request_and_response.Response;

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
