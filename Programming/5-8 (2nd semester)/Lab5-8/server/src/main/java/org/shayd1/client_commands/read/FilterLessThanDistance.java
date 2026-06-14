package org.shayd1.client_commands.read;

import org.shayd1.client_commands.ClientCommand;
import org.shayd1.manager.CollectionManager;
import org.shayd1.model.Route;
import org.shayd1.request_and_response.Response;

import java.util.List;

public class FilterLessThanDistance implements ClientCommand {

    private final CollectionManager collectionManager;

    public FilterLessThanDistance(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(String arg, Route route, Long userId) {
        if (arg == null || arg.isBlank()) {
            return new Response(false, "Usage: filter_less_than_distance <distance>");
        }

        List<Route> out;
        double distance;
        try {
            distance = Double.parseDouble(arg);
            out = collectionManager.filterLessThanDistance(distance);
        } catch (NumberFormatException ex) {
            return new Response(false, "Usage: filter_less_than_distance <distance>");
        }

        return new Response(true, "Routes with distance < " + distance + ":", out);
    }

    @Override
    public String getName() {
        return "filter_less_than_distance";
    }

    @Override
    public String getDescription() {
        return "shows elements, which distance field\nis less than inserted value";
    }
}
