package org.Gh0st1yAnge1.client_commands.write;

import org.Gh0st1yAnge1.client_commands.Command;
import org.Gh0st1yAnge1.request_and_response.CommandType;
import org.Gh0st1yAnge1.request_and_response.Request;
import org.Gh0st1yAnge1.utils.RouteBuilder;

public class ReplaceIfLower implements Command {

    private final RouteBuilder routeBuilder;

    public ReplaceIfLower(RouteBuilder routeBuilder){
        this.routeBuilder = routeBuilder;
    }

    @Override
    public Request execute(String arg) {
        return new Request(CommandType.REPLACE_IF_LOWER, arg, routeBuilder.buildRoute());
    }
}
