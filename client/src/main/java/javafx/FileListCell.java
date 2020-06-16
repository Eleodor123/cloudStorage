package javafx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import java.io.File;
import java.io.IOException;

public class FileListCell extends ListCell<File> {
    private FXMLLoader loader;

    @FXML
    public HBox vbPane;

    @FXML
    private ImageView folderImage;

    @FXML
    public Label nameLabel;

    @Override
    public void updateSelected(boolean selected) {
        super.updateSelected(selected);
    }

    @Override
    protected void updateItem(File item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (loader == null) {
                loader = new FXMLLoader(getClass().getResource("/CellItem.fxml"));
                loader.setController(this);
                try {
                    loader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            nameLabel.setText(item.getName());
            if(item.isDirectory()){
                folderImage.setVisible(true);
            } else {
                folderImage.setVisible(false);
            }
            setText(null);
            setGraphic(vbPane);
        }
    }
}