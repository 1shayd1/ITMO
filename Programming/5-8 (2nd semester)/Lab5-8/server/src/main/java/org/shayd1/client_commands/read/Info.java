package org.shayd1.client_commands.read;

import org.shayd1.client_commands.ClientCommand;
import org.shayd1.manager.CollectionManager;
import org.shayd1.model.Route;
import org.shayd1.request_and_response.Response;

public class Info implements ClientCommand {
    private final CollectionManager collectionManager;

    public Info(CollectionManager collectionManager){
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(String arg, Route route, Long userId)
    {
        if (arg != null){
            return new Response(false, "Usage: info");
        }
        return new Response(true, collectionManager.info()) ;
    }

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getDescription() {
        return "shows info about collection";
    }
}
