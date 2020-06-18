package javafx;

import control.StorageControl;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utils.Item;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

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
    Label noticeLabel;

    private StorageControl storageClient;
    private String login;
    private String password;
    private final String CLIENT_DEFAULT_DIR = "";
    private final String STORAGE_DEFAULT_DIR = "";
    private Item clientDefaultDirItem, storageDefaultDirItem;
    private Item clientCurrentDirItem, storageCurrentDirItem;
    private String newName = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        storageClient = new StorageControl(Controller.this);
        clientDefaultDirItem = new Item(CLIENT_DEFAULT_DIR);
        storageDefaultDirItem = new Item(STORAGE_DEFAULT_DIR);
        noticeLabel.setText("Connecting to the Cloud Storage server, please wait..");
        initializeClientItemListView();
        initializeStorageItemListView();
        new Thread(() -> {
            try {
                storageClient.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void openAuthWindow() {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent root = loader.load();
            LoginController loginController = loader.getController();
            loginController.backController = this;

            stage.setOnCloseRequest(event -> {

                System.out.println("stage.setOnCloseRequest...");
            });

            stage.setTitle("Authorisation to the Cloud Storage");
            stage.setScene(new Scene(root, 300, 200));
            stage.isAlwaysOnTop();
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startAuthorisation() {
        storageClient.startAuthorization();
    }

    public void initializeClientItemListView() {
        updateClientItemListInGUI(clientDefaultDirItem);
    }

    public void initializeStorageItemListView() {
        updateStorageItemListInGUI(new Item(STORAGE_DEFAULT_DIR),
                new Item[]{new Item("waiting for an item list from the server...",
                        "", "waiting for an item list from the server...",
                        "", false)});
    }

    public void updateClientItemListInGUI(Item directoryItem) {
        clientCurrentDirItem = directoryItem;
        Platform.runLater(() -> {
            clientDirLabel.setText(">>" + clientCurrentDirItem.getItemPathname());
            updateListView(clientItemListView, storageClient.clientItemsList(clientCurrentDirItem));
        });
    }

    public void updateStorageItemListInGUI(Item directoryItem, Item[] items){
        storageCurrentDirItem = directoryItem;
        Platform.runLater(() -> {
            storageDirLabel.setText(">>" + storageCurrentDirItem.getItemPathname());
            updateListView(storageItemListView, items);
        });
    }

    private void updateListView(ListView<Item> listView, Item[] items) {
        listView.getItems().clear();
        listView.getItems().addAll(items);
        listView.setCellFactory(itemListView -> new FileListCell());
        setContextMenu(listView);
    }

    private void setContextMenu(ListView<Item> listView){
        ContextMenu contextMenu = new ContextMenu();
        if(listView.equals(clientItemListView)){
            contextMenu.getItems().add(menuItemUpload(listView));
        } else if(listView.equals(storageItemListView)){
            contextMenu.getItems().add(menuItemDownload(listView));
        }
        contextMenu.getItems().addAll(menuItemRename(listView), menuItemDelete(listView));
        MenuItem menuItem = menuItemGetInto(listView);
        listView.setOnContextMenuRequested(event -> {
            if(contextMenu.isShowing() ||
                    listView.getSelectionModel().getSelectedItems().isEmpty()){
                contextMenu.hide();
                listView.getSelectionModel().clearSelection();
                return;
            }
            if(listView.getSelectionModel().getSelectedItem().isDirectory()){
                if(!contextMenu.getItems().contains(menuItem)){
                    contextMenu.getItems().add(0, menuItem);
                }
            } else {
                contextMenu.getItems().remove(menuItem);
            }
            contextMenu.show(listView, event.getScreenX(), event.getScreenY());
        });
    }

    private MenuItem menuItemGetInto(ListView<Item> listView) {
        MenuItem menuItemGetInto = new MenuItem("Get into");
        menuItemGetInto.setOnAction(event -> {
            Item item = listView.getSelectionModel().getSelectedItem();
            if(listView.equals(clientItemListView)){
                updateClientItemListInGUI(item);
            } else if(listView.equals(storageItemListView)){
                storageClient.demandDirectoryItemList(item.getItemPathname());
            }
            listView.getSelectionModel().clearSelection();
        });
        return menuItemGetInto;
    }

    private MenuItem menuItemUpload(ListView<Item> listView) {
        MenuItem menuItemUpload = new MenuItem("Upload");
        menuItemUpload.setOnAction(event -> {
            Item item = listView.getSelectionModel().getSelectedItem();
            try {
                storageClient.demandUploadItem(storageCurrentDirItem, item);
            } catch (IOException e) {
                e.printStackTrace();
            }
            listView.getSelectionModel().clearSelection();
        });
        return menuItemUpload;
    }

    private MenuItem menuItemDownload(ListView<Item> listView) {
        MenuItem menuItemDownload = new MenuItem("Download");
        menuItemDownload.setOnAction(event -> {
            Item item = listView.getSelectionModel().getSelectedItem();
            storageClient.demandDownloadItem(storageCurrentDirItem, clientCurrentDirItem, item);
            listView.getSelectionModel().clearSelection();
        });
        return menuItemDownload;
    }

    private MenuItem menuItemRename(ListView<Item> listView) {
        MenuItem menuItemRename = new MenuItem("Rename");
        menuItemRename.setOnAction(event -> {
            Item origin = listView.getSelectionModel().getSelectedItem();
            if(!takeNewNameWindow(origin)){
                return;
            }

            if(listView.equals(clientItemListView)){
                if(!storageClient.renameClientItem(origin, newName)){
                    System.out.println("Controller.menuItemRename() - Something wrong with item renaming!");
                }
                updateClientItemListInGUI(clientCurrentDirItem);
            } else if(listView.equals(storageItemListView)){
                storageClient.demandRenameItem(storageCurrentDirItem, origin, newName);
            }
            listView.getSelectionModel().clearSelection();
            newName = "";
        });
        return menuItemRename;
    }

    private boolean takeNewNameWindow(Item origin) {
        AtomicBoolean flag = new AtomicBoolean(false);
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Rename.fxml"));
            Parent root = loader.load();
            RenameController renameController = loader.getController();

            stage.setOnCloseRequest(event -> {
                System.out.println("Controller.menuItemRename() - " +
                        "the newNameWindow was closed forcibly!");
                flag.set(false);
            });

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
            return false;
        }
        return true;
    }

    private MenuItem menuItemDelete(ListView<Item> listView) {
        MenuItem menuItemDelete = new MenuItem("Delete");
        menuItemDelete.setOnAction(event -> {
            Item item = listView.getSelectionModel().getSelectedItem();
            if(listView.equals(clientItemListView)){
                if(!storageClient.deleteClientItem(item)){
                    System.out.println("Controller.menuItemRename() - Something wrong with item deleting!");
                }
                updateClientItemListInGUI(clientCurrentDirItem);
            } else if(listView.equals(storageItemListView)){
                storageClient.demandDeleteItem(storageCurrentDirItem, item);
            }
            listView.getSelectionModel().clearSelection();
        });
        return menuItemDelete;
    }

    @FXML
    public void onClientHomeBtnClicked(MouseEvent mouseEvent) {
        updateClientItemListInGUI(clientDefaultDirItem);
    }

    @FXML
    public void onStorageHomeBtnClicked(MouseEvent mouseEvent) {
        storageClient.demandDirectoryItemList(STORAGE_DEFAULT_DIR);
    }

    @FXML
    public void onClientGoUpBtnClicked(MouseEvent mouseEvent) {
        updateClientItemListInGUI(storageClient.getParentDirItem(
                clientCurrentDirItem, clientDefaultDirItem,
                StorageControl.CLIENT_ROOT));
    }

    @FXML
    public void onStorageGoUpBtnClicked(MouseEvent mouseEvent) {
        storageClient.demandDirectoryItemList(storageCurrentDirItem.getParentPathname());
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public void setLogin(String text) {
        login = text;
    }

    public void setPassword(String text) {
        password = text;
    }

    public void showTextInGUI(String text){
        Platform.runLater(() -> {
            noticeLabel.setText(text);
        });
    }

    public void openAuthWindowInGUI() {
        Platform.runLater(this::openAuthWindow);
    }

    public void dispose() {
        System.out.println("Отправляем сообщение о закрытии");
    }
}
