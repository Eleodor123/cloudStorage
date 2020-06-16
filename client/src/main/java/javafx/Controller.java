package javafx;

import control.StorageControl;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import utils.Item;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    Button  clientHomeButton, storageHomeButton,
            clientGoUpButton, storageGoUpButton,
            clientNewFolderButton, storageNewFolderButton;
    @FXML
    Label clientDirLabel, storageDirLabel;

    @FXML
    ListView<Item> clientItemListView, storageItemListView;

    @FXML
    Label label;

    private StorageControl storageControl;
    private final String CLIENT_DEFAULT_DIR = "";
    private final String STORAGE_DEFAULT_DIR = "";
    private Item clientDefaultDirItem, storageDefaultDirItem;
    private Item clientCurrentDirItem, storageCurrentDirItem;
    private String newName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        storageControl = new StorageControl(Controller.this);
        clientDefaultDirItem = new Item(CLIENT_DEFAULT_DIR);
        storageDefaultDirItem = new Item(STORAGE_DEFAULT_DIR);
        initializeClientItemListView();
        initializeStorageItemListView();
    }

    public void initializeClientItemListView() {
        //выводим в клиентской части интерфейса список объектов в директории по умолчанию
        updateClientItemListInGUI(clientDefaultDirItem);
    }

    public void initializeStorageItemListView() {
        updateStorageItemListInGUI(new Item(STORAGE_DEFAULT_DIR),
                new Item[]{new Item("waiting for an item list from the server...",
                        "", "waiting for an item list from the server...",
                        "", false)});
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    storageControl.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void updateClientItemListInGUI(Item directoryItem) {
        clientCurrentDirItem = directoryItem;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                clientDirLabel.setText(">>" + clientCurrentDirItem.getItemPathname());
                Controller.this.updateListView(clientItemListView, storageControl.clientItemsList(clientCurrentDirItem));
            }
        });
    }

    public void updateStorageItemListInGUI(Item directoryItem, final Item[] items){
        storageCurrentDirItem = directoryItem;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                storageDirLabel.setText(">>" + storageCurrentDirItem.getItemPathname());
                Controller.this.updateListView(storageItemListView, items);
            }
        });
    }

    private void updateListView(ListView<Item> listView, Item[] items) {
        listView.getItems().clear();
        listView.getItems().addAll(items);
        listView.setCellFactory(new Callback<ListView<Item>, ListCell<Item>>() {
            @Override
            public ListCell<Item> call(ListView<Item> itemListView) {
                return new ListCell();
            }
        });
        setContextMenu(listView);
    }

    private void setContextMenu(final ListView<Item> listView){
        final ContextMenu contextMenu = new ContextMenu();
        if(listView.equals(clientItemListView)){
            contextMenu.getItems().add(menuItemUpload(listView));
        } else if(listView.equals(storageItemListView)){
            contextMenu.getItems().add(menuItemDownload(listView));
        }
        contextMenu.getItems().addAll(menuItemRename(listView), menuItemDelete(listView));
        final MenuItem menuItem = menuItemGetInto(listView);
        listView.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent event) {
                if (contextMenu.isShowing() ||
                        listView.getSelectionModel().getSelectedItems().isEmpty()) {
                    contextMenu.hide();
                    listView.getSelectionModel().clearSelection();
                    return;
                }
                if (listView.getSelectionModel().getSelectedItem().isDirectory()) {
                    if (!contextMenu.getItems().contains(menuItem)) {
                        contextMenu.getItems().add(0, menuItem);
                    }
                } else {
                    contextMenu.getItems().remove(menuItem);
                }
                contextMenu.show(listView, event.getScreenX(), event.getScreenY());
            }
        });
    }

    private MenuItem menuItemGetInto(final ListView<Item> listView) {
        MenuItem menuItemGetInto = new MenuItem("Get into");
        menuItemGetInto.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Item item = listView.getSelectionModel().getSelectedItem();
                if (listView.equals(clientItemListView)) {
                    Controller.this.updateClientItemListInGUI(item);
                } else if (listView.equals(storageItemListView)) {
                    storageControl.demandDirectoryItemList(item.getItemPathname());
                }
                listView.getSelectionModel().clearSelection();
            }
        });
        return menuItemGetInto;
    }

    private MenuItem menuItemUpload(final ListView<Item> listView) {
        MenuItem menuItemUpload = new MenuItem("Upload");
        menuItemUpload.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Item item = listView.getSelectionModel().getSelectedItem();
                try {
                    storageControl.demandUploadItem(storageCurrentDirItem, item);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                listView.getSelectionModel().clearSelection();
            }
        });
        return menuItemUpload;
    }

    private MenuItem menuItemDownload(final ListView<Item> listView) {
        MenuItem menuItemDownload = new MenuItem("Download");
        menuItemDownload.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Item item = listView.getSelectionModel().getSelectedItem();
                storageControl.demandDownloadItem(storageCurrentDirItem, clientCurrentDirItem, item);
                listView.getSelectionModel().clearSelection();
            }
        });
        return menuItemDownload;
    }

    private MenuItem menuItemRename(final ListView<Item> listView) {
        MenuItem menuItemRename = new MenuItem("Rename");
        menuItemRename.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Item origin = listView.getSelectionModel().getSelectedItem();
                String newName = Controller.this.takeNewNameWindow(origin);
                if (listView.equals(clientItemListView)) {
                    if (!storageControl.renameClientItem(origin, newName)) {
                        System.out.println("Controller.menuItemRename() - Some thing wrong with item renaming!");
                    }
                    Controller.this.updateClientItemListInGUI(clientCurrentDirItem);
                } else if (listView.equals(storageItemListView)) {
                    storageControl.demandRenameItem(storageCurrentDirItem, origin, newName);
                }
                listView.getSelectionModel().clearSelection();
            }
        });
        return menuItemRename;
    }

    private String takeNewNameWindow(Item origin) {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/rename.fxml"));
            Parent root = loader.load();
            RenameController renameController = loader.getController();

            renameController.newName.setText(origin.getItemName());
            renameController.backController = this;

            stage.setTitle("insert a new name");
            stage.setScene(new Scene(root, 200, 50));
            stage.isAlwaysOnTop();
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newName;
    }

    private MenuItem menuItemDelete(final ListView<Item> listView) {
        MenuItem menuItemDelete = new MenuItem("Delete");
        menuItemDelete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Item item = listView.getSelectionModel().getSelectedItem();
                if (listView.equals(clientItemListView)) {
                    if (!storageControl.deleteClientItem(item)) {
                        System.out.println("GUIController.menuItemRename() - Some thing wrong with item deleting!");
                    }
                    Controller.this.updateClientItemListInGUI(clientCurrentDirItem);
                } else if (listView.equals(storageItemListView)) {
                    storageControl.demandDeleteItem(storageCurrentDirItem, item);
                }
                listView.getSelectionModel().clearSelection();
            }
        });
        return menuItemDelete;
    }

    @FXML
    public void onClientHomeBtnClicked(MouseEvent mouseEvent) {
        updateClientItemListInGUI(clientDefaultDirItem);
    }

    @FXML
    public void onStorageHomeBtnClicked(MouseEvent mouseEvent) {
        storageControl.demandDirectoryItemList(STORAGE_DEFAULT_DIR);
    }

    @FXML
    public void onClientGoUpBtnClicked(MouseEvent mouseEvent) {
        updateClientItemListInGUI(storageControl.getParentDirItem(
                clientCurrentDirItem, clientDefaultDirItem,
                StorageControl.CLIENT_ROOT));
    }

    @FXML
    public void onStorageGoUpBtnClicked(MouseEvent mouseEvent) {
        storageControl.demandDirectoryItemList(storageCurrentDirItem.getParentPathname());
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public void dispose() {
        System.out.println("Closing");
    }

}
