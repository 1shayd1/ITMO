package org.Gh0st1yAnge1.server_commands;

import org.Gh0st1yAnge1.manager.ServerCommandExecutor;

public class ServerHelp implements ServerCommand{

    private final ServerCommandExecutor serverCommandExecutor;

    public ServerHelp(ServerCommandExecutor serverCommandExecutor){this.serverCommandExecutor = serverCommandExecutor;}

    @Override
    public String getName() {
        return "help_server";
    }

    @Override
    public String getDescription() {
        return "shows available commands";
    }

    @Override
    public String execute(String arg) {
        String answer = "Available commands:\n";
        for (ServerCommand serverCommand: serverCommandExecutor.getServerCommands().values()){
            answer += "\n";
            answer += "--" + serverCommand.getName() + "--\n";
            answer += serverCommand.getDescription()+ "\n";
        }
        return answer;
    }
}