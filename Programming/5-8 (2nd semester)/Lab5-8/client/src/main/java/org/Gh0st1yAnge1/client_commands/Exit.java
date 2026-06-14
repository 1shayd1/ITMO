package org.Gh0st1yAnge1.client_commands;

import org.Gh0st1yAnge1.request_and_response.CommandType;
import org.Gh0st1yAnge1.request_and_response.Request;

public class Exit implements Command {

    @Override
    public Request execute(String args) {
        return new Request(CommandType.EXIT, args, null);
    }
}
