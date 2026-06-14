package org.Gh0st1yAnge1.client_commands.read;

import org.Gh0st1yAnge1.client_commands.ClientCommand;
import org.Gh0st1yAnge1.manager.CollectionManager;
import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.Response;

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
