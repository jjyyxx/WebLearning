package app;

import app.controls.CourseItem;
import app.controls.QuickButtonList;
import app.controls.SettingPane;
import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import common.AuthException;
import common.DataStore;
import common.Settings;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import platform.win.Imm32;
import weblearning.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static weblearning.Endpoints.authenticate;

public class Controller implements Initializable {
    private static final double ITEM_HEIGHT = 68.125;
    @FXML private ScrollPane courseListScrollPane;
    @FXML private JFXTreeTableView<Operation> workTable;
    @FXML private JFXTreeTableColumn<Operation, String> workName;
    @FXML private JFXTreeTableColumn<Operation, String> workDue;
    @FXML private JFXTreeTableColumn<Operation, Boolean> workDone;
    @FXML private JFXTreeTableView<FileEntry> fileTable;
    @FXML private JFXTreeTableColumn<FileEntry, String> fileCheck;
    @FXML private JFXTreeTableColumn<FileEntry, String> fileName;
    @FXML private JFXTreeTableColumn<FileEntry, String> fileDate;
    @FXML private JFXTreeTableColumn<FileEntry, String> fileSize;
    @FXML private JFXTreeTableColumn<FileEntry, Boolean> fileRead;
    @FXML private Label bulletinContent;
    @FXML private Label courseInfo;
    @FXML private JFXTreeTableView<Bulletin> bulletinTable;
    @FXML private JFXTreeTableColumn<Bulletin, String> bulletinTitle;
    @FXML private JFXTreeTableColumn<Bulletin, String> bulletinDate;
    @FXML private JFXTreeTableColumn<Bulletin, Boolean> bulletinRead;
    private double sceneX;
    private double sceneY;

    private Stage stage;
    private JFXSnackbar snackBar;

    @FXML private Pane root;
    @FXML private StackPane main;
    @FXML private Pane loginPane;
    @FXML private Pane coursePane;
    @FXML private QuickButtonList nodesList;
    @FXML private Pane mask;
    @FXML private StackPane spinner;
    @FXML private JFXListView<CourseItem> courseList;
    @FXML private JFXToolbar TopBar;
    @FXML private JFXTextField username;
    @FXML private JFXPasswordField password;
    @FXML private JFXButton login;

    @FXML private void close(ActionEvent event) {
        stage.close();
        System.exit(0);
    }

    @FXML private void minimize(ActionEvent event) {
        stage.setIconified(true);
    }

    @FXML private void login(ActionEvent event) {
        String name = username.getText();
        String pass = password.getText();
        toggleSpinner(true);
        authenticate(name, pass)
                .thenAccept(ignored -> {
                    DataStore.put("username", name);
                    DataStore.putEncrypt("password", pass);
                    afterLogin();
                })
                .exceptionally(e -> {
                    toggleSpinner(false);
                    String text = e instanceof AuthException ? "用户名或密码不正确，请检查。" : "网络连接不可用，请检查。";
                    snackBar.show(text, "error", 3000);
                    return null;
                });
    }

    @FXML private void topBarDragging(MouseEvent event) {
        stage.setX(event.getScreenX() - sceneX);
        stage.setY(event.getScreenY() - sceneY);
    }

    @FXML private void topBarPressed(MouseEvent event) {
        sceneX = event.getSceneX();
        sceneY = event.getSceneY();
    }

    @Override public void initialize(URL location, ResourceBundle resources) {
        snackBar = new JFXSnackbar(root);

        ObservableList<Node> children = main.getChildren();
        children.forEach(node -> node.setVisible(false));
        children.get(children.size() - 1).setVisible(true);

        try {
            Class.forName("weblearning.Endpoints");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        password.focusedProperty().addListener((observable, oldValue, newValue) -> Imm32.set(newValue));
        if (Settings.INSTANCE.autologin.get()) {
            weblearning.Endpoints.authenticate(
                    DataStore.get("username", ""),
                    DataStore.getDecrypt("password", "")
            ).thenAccept(ignored -> afterLogin()).exceptionally(e -> {
                e.printStackTrace();
                return null;
            });
        }
    }

    private void afterLogin() {
        Endpoints.getCurriculum().thenAccept(courseData -> Platform.runLater(() -> {
            toggleSpinner(false);
            JFXScrollPane.smoothScrolling(courseListScrollPane);
            courseList.setPrefHeight(courseData.size() * ITEM_HEIGHT);
            ObservableList<CourseItem> items = courseList.getItems();
            for (CourseData each : courseData.values()) {
                items.add(new CourseItem(each));
            }

            // bulletin
            bulletinTitle.setCellValueFactory(p -> p.getValue().getValue().name);
            bulletinDate.setCellValueFactory(p -> p.getValue().getValue().time);
            bulletinRead.setCellValueFactory(p -> p.getValue().getValue().isRead); // TODO: shall be optimized

            // file
            fileCheck.setCellValueFactory(p -> p.getValue().getValue().title);
            fileName.setCellValueFactory(p -> p.getValue().getValue().title);
            fileDate.setCellValueFactory(p -> p.getValue().getValue().uploadTime);
            fileSize.setCellValueFactory(p -> p.getValue().getValue().size);
            fileRead.setCellValueFactory(p -> p.getValue().getValue().isRead);

            // work
            workName.setCellValueFactory(p -> p.getValue().getValue().title);
            workDue.setCellValueFactory(p -> p.getValue().getValue().deadline);
            workDone.setCellValueFactory(p -> p.getValue().getValue().isHandedIn);

            courseList.getSelectionModel().selectedItemProperty().addListener((o, oldValue, newValue) -> {
                bulletinContent.setText("");
                newValue.courseData.resolveInformation().thenAccept(information -> Platform.runLater(() -> courseInfo.setText(information.name + information.introduction)));
                newValue.courseData.resolveBulletins().thenAccept(bulletins -> Platform.runLater(() -> {
                    ObservableList<Bulletin> bulletinList = FXCollections.observableArrayList(Arrays.asList(bulletins));
                    final TreeItem<Bulletin> root = new RecursiveTreeItem<>(bulletinList, RecursiveTreeObject::getChildren);
                    bulletinTable.getSelectionModel().selectedItemProperty().addListener((o1, oV, nV) -> {
                        if (nV == null) {
                            bulletinContent.setText("");
                        } else {
                            nV.getValue().resolveContent().thenAccept(stringProperty -> Platform.runLater(() -> bulletinContent.setText(stringProperty.get())));
                        }
                    });
                    bulletinTable.setRoot(root);
                    bulletinTable.setShowRoot(false);
                    bulletinTable.setEditable(false);
                }));
                newValue.courseData.resolveFileEntries().thenAccept(stringMap -> Platform.runLater(() -> {
                    // FIXME: shall use group
                    List<FileEntry> fileEntries = new ArrayList<>();
                    for (FileEntry[] value : stringMap.values()) {
                        fileEntries.addAll(Arrays.asList(value));
                    }
                    ObservableList<FileEntry> fileList = FXCollections.observableArrayList(fileEntries);
                    final TreeItem<FileEntry> root = new RecursiveTreeItem<>(fileList, RecursiveTreeObject::getChildren);
                    fileTable.setRoot(root);
                    fileTable.setShowRoot(false);
                    fileTable.setEditable(false);
                }));
                newValue.courseData.resolveOperations().thenAccept(operations -> Platform.runLater(() -> {
                    ObservableList<Operation> operationList = FXCollections.observableArrayList(Arrays.asList(operations));
                    final TreeItem<Operation> root = new RecursiveTreeItem<>(operationList, RecursiveTreeObject::getChildren);
                    workTable.setRoot(root);
                    workTable.setShowRoot(false);
                    workTable.setEditable(false);
                }));
            });

            courseList.getSelectionModel().selectFirst();
        })).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
        Platform.runLater(() -> {
            snackBar.show("登录成功！", "success", 3000);
            switchPane(coursePane);
            // nodesList.setVisible(true);
            // TODO: remove comment later
        });
    }

    private void toggleSpinner(boolean show) {
        mask.setVisible(show);
        spinner.setVisible(show);
    }

    private void switchPane(Pane nextPane) {
        ObservableList<Node> children = main.getChildren();
        children.get(children.size() - 1).setVisible(false);
        nextPane.toFront();
        nextPane.setVisible(true);
    }

    void setStage(Stage stage) {
        if (this.stage == null) {
            this.stage = stage;
        }
    }

    @FXML public void openSetting(ActionEvent actionEvent) {
        JFXDialog setting = new JFXDialog();
        setting.setContent(new SettingPane(t -> setting.close()));
        setting.show(main);
    }
}
