package utils.handlers;

import messages.AuthMessage;

public class ServiceCommandHandler extends CommandHandler{
    private AuthMessage authMessage;

    public ServiceCommandHandler(AuthMessage authMessage) {
        this.authMessage = authMessage;
    }

    public AuthMessage getAuthMessage() {
        return authMessage;
    }

    public void isAuthorized(){

        System.out.println("(Client)ServiceCommandHandler.isAuthorized: true!");

    }
}
