package org.shayd1.client_commands.auth;

import org.shayd1.client_commands.ClientCommand;
import org.shayd1.model.Route;
import org.shayd1.request_and_response.Response;

public class Logout implements ClientCommand {

    @Override
    public Response execute(String arg, Route route, Long userId) {
        return new Response(true, "Logged out!");
    }

    @Override
    public String getName() {
        return "logout";
    }

    @Override
    public String getDescription() {
        return "ends your current session";
    }
}
