package org.Gh0st1yAnge1.client_commands.auth;

import org.Gh0st1yAnge1.auth.AuthService;
import org.Gh0st1yAnge1.client_commands.ClientCommand;
import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.Response;

public class Register implements ClientCommand {

    private final AuthService authService;

    public Register(AuthService authService){
        this.authService = authService;
    }

    @Override
    public Response execute(String arg, Route route, Long userId) {
        if (arg == null || arg.trim().isEmpty()) {
            return new Response(false, "Usage: register <login> <password>");
        }
        String[] parts = arg.trim().split("\\s+");
        if (parts.length != 2){
            return new Response(false, "Usage: register <login> <password>");
        }

        String result = authService.register(parts[0], parts[1]);

        boolean success = !result.toLowerCase().contains("already")
                && !result.toLowerCase().contains("error")
                && !result.toLowerCase().contains("fail");
        return new Response(success, result);
    }

    @Override
    public String getName() {
        return "register";
    }

    @Override
    public String getDescription() {
        return "register <login> <password> — create a new account\"";
    }
}
