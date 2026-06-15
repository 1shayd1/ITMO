package org.shayd1.client_commands.read;

import org.shayd1.client_commands.ClientCommand;
import org.shayd1.manager.CollectionManager;
import org.shayd1.model.Route;
import org.shayd1.request_and_response.Response;

public class CountByDistance implements ClientCommand {

    private final CollectionManager collectionManager;

    public CountByDistance(CollectionManager collectionManager){
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(String arg, Route route, Long userId) {

        if (arg == null || arg.isBlank()){
            return new Response(false, "Usage: count_by_distance <distance>");
        }

        double distance;
        long count;

        try{
            distance = Double.parseDouble(arg);
            count =  collectionManager.countByDistance(distance);
        } catch (NumberFormatException ex){
            return new Response(false, "Usage: count_by_distance <distance>");
        }

        return new Response(true, count + "");
    }

    @Override
    public String getName() {
        return "count_by_distance";
    }

    @Override
    public String getDescription() {
        return "shows number of elements, which distance fields\nare equals to inserted value";
    }
}
