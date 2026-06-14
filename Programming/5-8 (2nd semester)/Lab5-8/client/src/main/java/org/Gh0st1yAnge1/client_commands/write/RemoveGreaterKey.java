package org.Gh0st1yAnge1.client_commands.write;

import org.Gh0st1yAnge1.client_commands.Command;
import org.Gh0st1yAnge1.request_and_response.CommandType;
import org.Gh0st1yAnge1.request_and_response.Request;

public class RemoveGreaterKey implements Command {

    @Override
    public Request execute(String args) {
        return new Request(CommandType.REMOVE_GREATER_KEY, args, null);
    }
}
