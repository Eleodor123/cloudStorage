package javafx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import utils.Item;

import java.io.IOException;

public class FileListCell extends ListCell<Item> {
    private FXMLLoader mLLoader;

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
    protected void updateItem(Item item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (mLLoader == null) {
                mLLoader = new FXMLLoader(getClass().getResource("/Cell.fxml"));
                mLLoader.setController(this);
                try {
                    mLLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            nameLabel.setText(item.getItemName());
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
