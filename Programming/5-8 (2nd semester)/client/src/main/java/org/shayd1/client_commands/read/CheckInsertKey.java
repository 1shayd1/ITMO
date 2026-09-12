package org.shayd1.client_commands.read;

import org.shayd1.client_commands.Command;
import org.shayd1.request_and_response.CommandType;
import org.shayd1.request_and_response.Request;

public class CheckInsertKey implements Command {

    @Override
    public Request execute(String args) {
        return new Request(CommandType.CHECK_INSERT_KEY, args, null);
    }
}
