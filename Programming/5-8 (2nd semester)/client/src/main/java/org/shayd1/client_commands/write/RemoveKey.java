package org.shayd1.client_commands.write;

import org.shayd1.client_commands.Command;
import org.shayd1.request_and_response.CommandType;
import org.shayd1.request_and_response.Request;

public class RemoveKey implements Command {

    @Override
    public Request execute(String arg) {
        return new Request(CommandType.REMOVE_KEY, arg, null);
    }
}
