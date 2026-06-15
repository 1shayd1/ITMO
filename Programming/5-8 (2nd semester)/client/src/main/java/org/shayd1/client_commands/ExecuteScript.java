package org.shayd1.client_commands;


import org.shayd1.request_and_response.CommandType;
import org.shayd1.request_and_response.Request;

public class ExecuteScript implements Command{

    @Override
    public Request execute(String arg) {
        return new Request(CommandType.EXECUTE_SCRIPT, arg, null);
    }
}
