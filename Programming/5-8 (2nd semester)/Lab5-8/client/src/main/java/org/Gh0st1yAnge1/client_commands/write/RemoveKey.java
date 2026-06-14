package org.Gh0st1yAnge1.client_commands.write;

import org.Gh0st1yAnge1.client_commands.Command;
import org.Gh0st1yAnge1.request_and_response.CommandType;
import org.Gh0st1yAnge1.request_and_response.Request;

public class RemoveKey implements Command {

    @Override
    public Request execute(String arg) {
        return new Request(CommandType.REMOVE_KEY, arg, null);
    }
}
