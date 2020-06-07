package utils;

import messages.AbstractMessage;

import java.io.Serializable;

public class CommandMessage implements Serializable {
    private int command;
    private AbstractMessage messageObject;

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
}