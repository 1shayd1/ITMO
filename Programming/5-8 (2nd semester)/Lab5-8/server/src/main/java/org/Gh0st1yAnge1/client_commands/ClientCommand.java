package org.Gh0st1yAnge1.client_commands;

import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.Response;

public interface ClientCommand {
    String getName();
    String getDescription();
    Response execute(String arg, Route route, Long userId);
}
