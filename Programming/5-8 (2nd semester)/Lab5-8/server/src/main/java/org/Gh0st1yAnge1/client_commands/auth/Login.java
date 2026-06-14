package org.Gh0st1yAnge1.client_commands.auth;

import org.Gh0st1yAnge1.auth.AuthService;
import org.Gh0st1yAnge1.client_commands.ClientCommand;
import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.Response;

import java.sql.SQLException;

public class Login implements ClientCommand {

    private final AuthService authService;

    public Login(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public Response execute(String arg, Route route, Long userId) {
        if (arg == null || arg.trim().isEmpty()) {
            return new Response(false, "Usage: login <login> <password>");
        }
        String[] parts = arg.trim().split("\\s+");
        if (parts.length != 2) {
            return new Response(false, "Usage: login <login> <password>");
        }

        try {
            Long authenticatedId = authService.login(parts[0], parts[1]);

            if (authenticatedId != null) {
                // ВМЕСТО текста возвращаем ID пользователя в виде строки!
                return new Response(true, String.valueOf(authenticatedId));
            }
            return new Response(false, "Invalid login or password.");

        } catch (SQLException e) {
            return new Response(false, "Database error: Cannot authenticate right now. Server database is unavailable.");
        }
    }

    @Override
    public String getName()        { return "login"; }
    @Override
    public String getDescription() { return "login <login> <password> — authenticate"; }
}