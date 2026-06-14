package org.shayd1.client_commands.write;

import org.shayd1.client_commands.Command;
import org.shayd1.request_and_response.CommandType;
import org.shayd1.request_and_response.Request;
import org.shayd1.utils.RouteBuilder;

public class Insert implements Command {

    private final RouteBuilder routeBuilder;

    public Insert(RouteBuilder routeBuilder){
        this.routeBuilder = routeBuilder;
    }

    @Override
    public Request execute(String arg) {
        return new Request(CommandType.INSERT, arg, routeBuilder.buildRoute());
    }
}
