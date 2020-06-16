package javafx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class RenameController {

    @FXML
    TextField newName;

    @FXML
    VBox globParent;

    public Controller backController;

    @FXML
    public void saveNewName(ActionEvent actionEvent) {
        backController.setNewName(newName.getText());
        globParent.getScene().getWindow().hide();
    }
}
