package org.Gh0st1yAnge1.client_commands.auth;

import org.Gh0st1yAnge1.client_commands.Command;
import org.Gh0st1yAnge1.request_and_response.CommandType;
import org.Gh0st1yAnge1.request_and_response.Request;

public class Logout implements Command {
    @Override
    public Request execute(String args) {
        return new Request(CommandType.LOGOUT, null, null, null, null);
    }
}
