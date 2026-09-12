package org.shayd1.client_commands;

import org.shayd1.request_and_response.CommandType;
import org.shayd1.request_and_response.Request;

public class Exit implements Command {

    @Override
    public Request execute(String args) {
        return new Request(CommandType.EXIT, args, null);
    }
}
