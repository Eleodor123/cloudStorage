package javafx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class LoginController {
    @FXML
    TextField login;

    @FXML
    PasswordField password;

    @FXML
    VBox globParent;

    public Controller backController;

    public void auth(ActionEvent actionEvent) {
        if(isLoginPasswordCorrect(login.getText(), password.getText())){
            backController.setLogin(login.getText());
            backController.setPassword(password.getText());
            backController.startAuthorisation();
            globParent.getScene().getWindow().hide();
        }
    }

    private boolean isLoginPasswordCorrect(String login, String password){

        System.out.println("LoginController.isLoginPasswordCorrect() - login: " + login
                + ", password: " + password);

        return !login.trim().isEmpty() && !password.trim().isEmpty();
    }

    public void onRegistrationLink(ActionEvent actionEvent) {
        System.out.println("LoginController.onRegistrationLink() - get registration");
    }

}
