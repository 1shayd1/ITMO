package org.shayd1.server_commands;

public interface ServerCommand {
    String getName();
    String getDescription();
    String execute(String arg);
}
