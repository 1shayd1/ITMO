package org.Gh0st1yAnge1.client_commands.auth;

import org.Gh0st1yAnge1.client_commands.Command;
import org.Gh0st1yAnge1.request_and_response.CommandType;
import org.Gh0st1yAnge1.request_and_response.Request;

public class Register implements Command {
    @Override
    public Request execute(String args) {
        if (args == null || args.trim().isEmpty()) {
            System.out.println("Usage: register <login> <password>");
            return null;
        }

        String[] parts = args.trim().split("\\s+");
        if (parts.length != 2) {
            System.out.println("Usage: register <login> <password>");
            return null;
        }
        return new Request(CommandType.REGISTER, null,null, parts[0], parts[1]);
    }
}
