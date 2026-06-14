package org.Gh0st1yAnge1.client_commands;


import org.Gh0st1yAnge1.request_and_response.CommandType;
import org.Gh0st1yAnge1.request_and_response.Request;

public class ExecuteScript implements Command{

    @Override
    public Request execute(String arg) {
        return new Request(CommandType.EXECUTE_SCRIPT, arg, null);
    }
}
