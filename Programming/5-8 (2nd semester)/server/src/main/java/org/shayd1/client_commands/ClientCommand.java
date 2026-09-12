package org.shayd1.client_commands;

import org.shayd1.model.Route;
import org.shayd1.request_and_response.Response;

public interface ClientCommand {
    String getName();
    String getDescription();
    Response execute(String arg, Route route, Long userId);
}
