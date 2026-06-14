package org.Gh0st1yAnge1.client_commands.read;

import org.Gh0st1yAnge1.client_commands.Command;
import org.Gh0st1yAnge1.request_and_response.CommandType;
import org.Gh0st1yAnge1.request_and_response.Request;

public class FilterLessThanDistance implements Command {

    @Override
    public Request execute(String arg) {
        return new Request(CommandType.FILTER_LESS_THAN_DISTANCE, arg, null);
    }
}
