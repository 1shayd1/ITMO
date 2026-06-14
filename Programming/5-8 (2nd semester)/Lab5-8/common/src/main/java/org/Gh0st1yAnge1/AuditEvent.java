package org.Gh0st1yAnge1;

import java.io.Serializable;
import java.time.Instant;

public class AuditEvent implements Serializable {

    private String commandType;
    private String argument;
    private String timestamp;
    private boolean success;
    private String message;

    public AuditEvent() {}

    public AuditEvent(String commandType, String argument, boolean success, String message) {
        this.commandType = commandType;
        this.argument    = argument;
        this.timestamp   = Instant.now().toString();
        this.success     = success;
        this.message     = message;
    }

    public String getCommandType() { return commandType; }
    public void setCommandType(String commandType) { this.commandType = commandType; }

    public String getArgument() { return argument; }
    public void setArgument(String argument) { this.argument = argument; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    @Override
    public String toString() {
        return "AuditEvent{commandType='" + commandType + "', argument='" + argument +
               "', timestamp='" + timestamp + "', success=" + success + '}';
    }
}
