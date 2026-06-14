package org.shayd1.client_commands.auth;

import org.shayd1.client_commands.Command;
import org.shayd1.request_and_response.CommandType;
import org.shayd1.request_and_response.Request;

public class Logout implements Command {
    @Override
    public Request execute(String args) {
        return new Request(CommandType.LOGOUT, null, null, null, null);
    }
}
