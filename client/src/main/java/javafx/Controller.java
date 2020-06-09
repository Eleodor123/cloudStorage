package javafx;

import control.StorageControl;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.util.Callback;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    Label clientDirLabel, storageDirLabel;
    @FXML
    ListView<File> clientItemListView, storageItemListView;
    @FXML
    Label label;
    private StorageControl storageClient;
    private String currentClientDir;
    private String currentStorageDir;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        storageClient = new StorageControl(Controller.this);
        currentClientDir = storageClient.getDefaultDirClient();
        currentStorageDir = storageClient.getDefaultDitServer();
        initializeClientItemListView();
        initializeStorageItemListView();
    }

    public void initializeClientItemListView() {
        updateClientItemListInGUI(clientDefaultDirectory(), clientFilesList(clientDefaultDirectory()));
    }

    public void initializeStorageItemListView() {
        updateStorageItemListInGUI("../",
                new File[]{new File("waiting for an item list from the server...")});
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    storageClient.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void updateClientItemListInGUI(String directory, final File[] fileObjs){
        currentClientDir = directory;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                clientDirLabel.setText(currentClientDir);
                Controller.this.updateListView(clientItemListView, fileObjs);
            }
        });
    }

    public void updateStorageItemListInGUI(String directory, final File[] fileObjs){
        currentStorageDir = directory;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                storageDirLabel.setText(currentStorageDir);
                Controller.this.updateListView(storageItemListView, fileObjs);
            }
        });
    }

    private void updateListView(ListView<File> listView, File[] fileObjs) {
        listView.getItems().clear();
        listView.getItems().addAll(fileObjs);
        listView.setCellFactory(new Callback<ListView<File>, ListCell<File>>() {
            @Override
            public ListCell<File> call(ListView<File> itemListView) {
                return new Cells();
            }
        });
        setContextMenu(listView);
    }

    private void setContextMenu(final ListView<File> listView){
        final ContextMenu contextMenu = new ContextMenu();
        if(listView.equals(clientItemListView)){
            contextMenu.getItems().add(menuItemUpload(listView));
        } else if(listView.equals(storageItemListView)){
            contextMenu.getItems().add(menuItemDownload(listView));
        }
        contextMenu.getItems().addAll(menuItemRename(listView), menuItemDelete(listView));
        final MenuItem menuItem = menuItemGetList(listView);
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

    private MenuItem menuItemGetList(final ListView<File> listView) {
        MenuItem menuItemGetList = new MenuItem("GetList");
        menuItemGetList.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                File item = listView.getSelectionModel().getSelectedItem();
                if (listView.equals(clientItemListView)) {

                    System.out.println("Controller.onClickClientListFolderItem() - " +
                            ", listView.getSelectionModel().getSelectedItem().getName(): " +
                            listView.getSelectionModel().getSelectedItem().getName()
                    );
                    Controller.this.updateClientItemListInGUI(item.getName(), item.listFiles());
                } else if (listView.equals(storageItemListView)) {
                    storageClient.demandDirectoryItemList(item.getName());
                }
                listView.getSelectionModel().clearSelection();
            }
        });
        return menuItemGetList;
    }

    private MenuItem menuItemUpload(final ListView<File> listView) {
        MenuItem menuItemUpload = new MenuItem("Upload");
        menuItemUpload.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Controller.callContextMenu().menuItemUpload.setOnAction() - " +
                        "\nlistView.getSelectionModel().getSelectedItem(): " +
                        listView.getSelectionModel().getSelectedItem());

                listView.getSelectionModel().clearSelection();
            }
        });
        return menuItemUpload;
    }

    private MenuItem menuItemDownload(final ListView<File> listView) {
        MenuItem menuItemDownload = new MenuItem("Download");
        menuItemDownload.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Controller.callContextMenu().menuItemDownload.setOnAction() - " +
                        "\nlistView.getSelectionModel().getSelectedItem(): " +
                        listView.getSelectionModel().getSelectedItem());

                listView.getSelectionModel().clearSelection();
            }
        });
        return menuItemDownload;
    }

    private MenuItem menuItemRename(final ListView<File> listView) {
        MenuItem menuItemRename = new MenuItem("Rename");
        menuItemRename.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                System.out.println("Controller.callContextMenu().menuItemRename.setOnAction() - " +
                        "\nlistView.getSelectionModel().getSelectedItem(): " +
                        listView.getSelectionModel().getSelectedItem());

                File origin = listView.getSelectionModel().getSelectedItem();

                String newName = "Renamed " + origin.getName();

                System.out.println("Controller.callContextMenu().menuItemRename.setOnAction() - " +
                        "origin.renameTo()): " +
                        origin.renameTo(new File(Paths.get(origin.getParent(),
                                newName).toString())));

                listView.getSelectionModel().clearSelection();
                Controller.this.updateClientItemListInGUI(origin.getParent(), new File(origin.getParent()).listFiles());
            }
        });
        return menuItemRename;
    }

    private MenuItem menuItemDelete(final ListView<File> listView) {
        MenuItem menuItemDelete = new MenuItem("Delete");
        menuItemDelete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Controller.callContextMenu().menuItemDelete.setOnAction() - " +
                        "\nlistView.getSelectionModel().getSelectedItem(): " +
                        listView.getSelectionModel().getSelectedItem());

                File origin = listView.getSelectionModel().getSelectedItem();
                if (origin.isDirectory()) {
                    storageClient.deleteFolder(origin);
                } else {
                    System.out.println("Controller.callContextMenu().menuItemDelete.setOnAction() - " +
                            "origin.delete(): " + origin.delete());
                }
                listView.getSelectionModel().clearSelection();
                Controller.this.updateClientItemListInGUI(origin.getParent(), new File(origin.getParent()).listFiles());
            }
        });
        return menuItemDelete;
    }

    private String clientDefaultDirectory(){
        return storageClient.getDefaultDirClient();
    }

    private File[] clientFilesList(String currentDirectory) {
        return new File(realClientDirectory(currentDirectory)).listFiles();
    }

    private String realClientDirectory(String currentDirectory){
        return Paths.get(StorageControl.CLIENT_ROOT, currentDirectory).toString();
    }

    public void dispose() {
        System.out.println("Client closed");
    }


//    @FXML
//    ListView<String> clientFiles, serverFiles;
//
//    @FXML
//    Label label;
//
//    @Override
//    public void initialize(URL location, ResourceBundle resources) {
//        clientFiles.getItems().addAll("C_File 1", "C_File 2", "C_File 3");
//        serverFiles.getItems().addAll("S_File 1", "S_File 2", "S_File 3");
//    }
//
//    public void btnClickSelectedClientFile(ActionEvent actionEvent) {
//        label.setText(clientFiles.getSelectionModel().getSelectedItem());
//    }
//
//    public void btnClickSelectedServerFile(ActionEvent actionEvent) {
//        label.setText(serverFiles.getSelectionModel().getSelectedItem());
//    }
}
