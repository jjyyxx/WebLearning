package app;

import app.controls.CourseItem;
import app.controls.InformationPane;
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
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import platform.win.Imm32;
import weblearning.*;

import java.net.URL;
import java.util.*;

import static weblearning.Endpoints.authenticate;

public class Controller implements Initializable {
    private static final double ITEM_HEIGHT = 68.125;
    public JFXToggleButton separateByCourse;
    public JFXToggleButton removePostfix;
    public JFXButton batchDownload;
    public JFXButton fileDownload;
    public Label fileName1;
    public Label fileDescription;

    public JFXTextArea workRequirement;
    public Hyperlink workRequirementAttachment;
    public JFXTextArea workContent;
    public JFXTextField workAttachment;
    public JFXButton workCommit;

    @FXML private InformationPane courseInfo;
    @FXML private ScrollPane courseListScrollPane;
    @FXML private JFXTreeTableView<Operation> workTable;
    @FXML private JFXTreeTableColumn<Operation, String> workName;
    @FXML private JFXTreeTableColumn<Operation, String> workDue;
    @FXML private JFXTreeTableColumn<Operation, String> workDone;
    @FXML private JFXTreeTableView<FileEntry> fileTable;
    @FXML private JFXTreeTableColumn<FileEntry, String> fileName;
    @FXML private JFXTreeTableColumn<FileEntry, String> fileDate;
    @FXML private JFXTreeTableColumn<FileEntry, String> fileSize;
    @FXML private JFXTreeTableColumn<FileEntry, String> fileRead;
    @FXML private Label bulletinContent;
    @FXML private Label bulletinTitle1;
    @FXML private JFXButton bulletAlertButton;
    @FXML private JFXTreeTableView<Bulletin> bulletinTable;
    @FXML private JFXTreeTableColumn<Bulletin, String> bulletinTitle;
    @FXML private JFXTreeTableColumn<Bulletin, String> bulletinDate;
    @FXML private JFXTreeTableColumn<Bulletin, String> bulletinRead;
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
    private boolean dirtyUpdateFlag = false;

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

        // bulletin
        bulletinRead.setCellValueFactory(p -> bulletinRead.validateValue(p) ? p.getValue().getValue().isRead : bulletinRead.getComputedValue(p)); // TODO: shall be optimized
        bulletinTitle.setCellValueFactory(p -> bulletinTitle.validateValue(p) ? p.getValue().getValue().name : bulletinTitle.getComputedValue(p));
        bulletinDate.setCellValueFactory(p -> bulletinDate.validateValue(p) ? p.getValue().getValue().time : bulletinDate.getComputedValue(p));
        bulletinTable.getSelectionModel().selectedItemProperty().addListener((o1, oV, nV) -> {
            if (dirtyUpdateFlag) return;
            if (nV == null) {
                bulletinContent.setText("");
                bulletinTitle1.setText("");
            } else {
                RecursiveTreeObject value = nV.getValue();
                if (value instanceof Bulletin) {
                    bulletinTitle1.setText(nV.getValue().name.get());
                    nV.getValue().resolveContent().thenAccept(stringProperty -> Platform.runLater(() -> bulletinContent.setText(stringProperty.get())));
                }
            }
        });

        // file
        fileTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        fileName.setCellValueFactory(p -> fileName.validateValue(p) ? p.getValue().getValue().title : fileName.getComputedValue(p));
        fileDate.setCellValueFactory(p -> fileDate.validateValue(p) ? p.getValue().getValue().uploadTime : fileSize.getComputedValue(p));
        fileSize.setCellValueFactory(p -> fileSize.validateValue(p) ? p.getValue().getValue().size : fileSize.getComputedValue(p));
        fileRead.setCellValueFactory(p -> fileRead.validateValue(p) ? p.getValue().getValue().isRead : fileRead.getComputedValue(p));
        fileTable.getSelectionModel().selectedItemProperty().addListener((o1, oV, nV) -> {
            if (dirtyUpdateFlag) return;
            if (nV == null) {
                fileDescription.setText("");
                fileName.setText("");
            } else {
                RecursiveTreeObject value = nV.getValue();
                if (value instanceof FileEntry) {
                    fileName1.setText(nV.getValue().title.get());
                    fileDescription.setText(nV.getValue().description.get());
                }
            }
        });

        // work
        workName.setCellValueFactory(p -> workName.validateValue(p) ? p.getValue().getValue().title : workName.getComputedValue(p));
        workDue.setCellValueFactory(p -> workDue.validateValue(p) ? p.getValue().getValue().deadline : workDue.getComputedValue(p));
        workDone.setCellValueFactory(p -> workDone.validateValue(p) ? p.getValue().getValue().isHandedIn : workDone.getComputedValue(p));
        workTable.getSelectionModel().selectedItemProperty().addListener((o1, oV, nV) -> {
            if (dirtyUpdateFlag) return;
            if (nV == null) {
                workRequirement.setText("");
                workRequirementAttachment.setDisable(true);
            } else {
                RecursiveTreeObject value = nV.getValue();
                if (value instanceof Operation) {
                    nV.getValue().resolveDetail().thenAccept(aVoid -> Platform.runLater(() -> {
                        workRequirement.setText(nV.getValue().getDescription());
                        if (nV.getValue().isAttachmentExists()) {
                            workRequirementAttachment.setText(nV.getValue().getAttachmentName());
                            workRequirementAttachment.setDisable(false);
                            workRequirementAttachment.setOnAction(actionEvent -> {
                                // nV.getValue().downloadRequirementAttachment();
                            });
                        } else {
                            workRequirementAttachment.setDisable(true);
                        }
                    }));
                }
            }
        });

        JFXScrollPane.smoothScrolling(courseListScrollPane);

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
            courseList.setPrefHeight(courseData.size() * ITEM_HEIGHT);
            ObservableList<CourseItem> items = courseList.getItems();
            for (CourseData each : courseData.values()) {
                items.add(new CourseItem(each));
            }

            courseList.getSelectionModel().selectedItemProperty().addListener((o, oldValue, newValue) -> {
                // info TODO: remove comment
                // newValue.courseData.resolveInformation().thenAccept(courseInfo::setInformation);

                // bulletin
                // bulletinContent.setText("");
                // bulletinTitle1.setText("");
                newValue.courseData.resolveBulletins().thenAccept(bulletins -> Platform.runLater(() -> {
                    dirtyUpdateFlag = true;
                    bulletinTable.unGroup(bulletinRead);
                    ObservableList<Bulletin> bulletinList = FXCollections.observableArrayList(Arrays.asList(bulletins));
                    final TreeItem<Bulletin> root = new RecursiveTreeItem<>(bulletinList, RecursiveTreeObject::getChildren);
                    bulletinTable.setRoot(root);
                    bulletinTable.setShowRoot(false);
                    bulletinTable.setEditable(false);
                    bulletinTable.group(bulletinRead);
                    for (TreeItem<Bulletin> child : bulletinTable.getRoot().getChildren()) {
                        child.setExpanded(true);
                    }
                    bulletinTable.getSortOrder().clear();
                    bulletinTable.getSortOrder().add(bulletinRead);
                    bulletinRead.setSortType(TreeTableColumn.SortType.ASCENDING);
                    bulletinRead.setSortable(true);
                    dirtyUpdateFlag = false;
                    bulletinTable.getSelectionModel().clearSelection();
                }));

                // file
                newValue.courseData.resolveFileEntries().thenAccept(stringMap -> Platform.runLater(() -> {
                    dirtyUpdateFlag = true;
                    fileTable.unGroup(fileRead);
                    List<FileEntry> fileEntries = new ArrayList<>();
                    for (Map.Entry<String, FileEntry[]> value : stringMap.entrySet()) {
                        fileEntries.addAll(Arrays.asList(value.getValue()));
                    }
                    ObservableList<FileEntry> fileList = FXCollections.observableArrayList(fileEntries);
                    final TreeItem<FileEntry> root = new RecursiveTreeItem<>(fileList, RecursiveTreeObject::getChildren);
                    fileTable.setRoot(root);
                    fileTable.setShowRoot(false);
                    fileTable.setEditable(false);
                    fileTable.group(fileRead);
                    for (TreeItem<FileEntry> child : fileTable.getRoot().getChildren()) {
                        child.setExpanded(true);
                    }
                    fileTable.getSortOrder().clear();
                    fileTable.getSortOrder().add(fileRead);
                    fileRead.setSortType(TreeTableColumn.SortType.ASCENDING);
                    fileRead.setSortable(true);
                    dirtyUpdateFlag = false;
                    fileTable.getSelectionModel().clearSelection();
                }));

                // operation
                newValue.courseData.resolveOperations().thenAccept(operations -> Platform.runLater(() -> {
                    dirtyUpdateFlag = true;
                    workTable.unGroup(workDone);
                    ObservableList<Operation> operationList = FXCollections.observableArrayList(Arrays.asList(operations));
                    final TreeItem<Operation> root = new RecursiveTreeItem<>(operationList, RecursiveTreeObject::getChildren);
                    workTable.setRoot(root);
                    workTable.setShowRoot(false);
                    workTable.setEditable(false);
                    workTable.group(workDone);
                    for (TreeItem<Operation> child : workTable.getRoot().getChildren()) {
                        child.setExpanded(true);
                    }
                    workTable.getSortOrder().clear();
                    workTable.getSortOrder().add(workDone);
                    workDone.setSortType(TreeTableColumn.SortType.ASCENDING);
                    workDone.setSortable(true);
                    dirtyUpdateFlag = false;
                    workTable.getSelectionModel().clearSelection();
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

    @FXML public void addAlert(ActionEvent actionEvent) {
    }
}
