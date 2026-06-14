package org.shayd1.client_commands;

import org.shayd1.request_and_response.Request;

public interface Command {
    Request execute(String args);
}
