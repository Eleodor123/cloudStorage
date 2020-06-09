package utils;

import messages.AbstractMessage;
import java.io.Serializable;

public class CommandMessage implements Serializable {
    private int command;
    private AbstractMessage messageObject;

    private String directory;

    public CommandMessage(int command) {
        this.command = command;
    }

    public CommandMessage(int command, String directory) {
        this.command = command;
        this.directory = directory;
    }

    public CommandMessage(int command, AbstractMessage messageObject) {
        this.command = command;
        this.messageObject = messageObject;
    }

    public int getCommand() {
        return command;
    }

    public AbstractMessage getMessageObject() {
        return messageObject;
    }

    public String getDirectory() {
        return directory;
    }
}