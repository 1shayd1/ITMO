package org.shayd1.client_commands.read;

import org.shayd1.client_commands.ClientCommand;
import org.shayd1.manager.ServerCommandExecutor;
import org.shayd1.model.Route;
import org.shayd1.request_and_response.Response;

public class Help implements ClientCommand {

    private final ServerCommandExecutor serverCommandExecutor;

    public Help(ServerCommandExecutor serverCommandExecutor){
        this.serverCommandExecutor = serverCommandExecutor;
    }


    @Override
    public Response execute(String arg, Route route, Long userId) {

        if (arg != null){
            return new Response(false, "Usage: help");
        }

        String answer = "Available commands:\n\n--execute_script--\nexecutes script\n";
        for (ClientCommand clientCommand : serverCommandExecutor.getCommands().values()){
            if (clientCommand.getName().equals("save") || clientCommand.getName().equals("check_key")){
                continue;
            }
            answer += " \n";
            answer += "--" + clientCommand.getName() + "--\n";
            answer += clientCommand.getDescription()+ "\n";
        }

        return new Response(true, answer);
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "shows available commands";
    }
}
