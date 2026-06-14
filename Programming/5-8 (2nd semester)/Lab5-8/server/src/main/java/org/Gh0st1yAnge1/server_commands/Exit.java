package org.Gh0st1yAnge1.server_commands;

public class Exit implements ServerCommand {

    public Exit(){}

    @Override
    public String getName() {
        return "exit";
    }

    @Override
    public String getDescription() {
        return "terimantes session";
    }

    @Override
    public String execute(String arg) {
        return "Session terminated.";
    }
}
