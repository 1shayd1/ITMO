package org.shayd1.client_commands.read;

import org.shayd1.client_commands.Command;
import org.shayd1.request_and_response.CommandType;
import org.shayd1.request_and_response.Request;

public class AverageOfDistance implements Command {

    @Override
    public Request execute(String arg) {
        return new Request(CommandType.AVERAGE_OF_DISTANCE, arg, null);
    }
}
