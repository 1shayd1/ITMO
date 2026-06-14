package org.Gh0st1yAnge1.server_commands;

public interface ServerCommand {
    String getName();
    String getDescription();
    String execute(String arg);
}
