package org.Gh0st1yAnge1.client_commands;

import org.Gh0st1yAnge1.request_and_response.Request;

public interface Command {
    Request execute(String args);
}
