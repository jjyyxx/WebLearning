package app;

import app.controls.CourseItem;
import app.controls.SettingPane;
import background.DownloadManager;
import background.Notification;
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

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static weblearning.Endpoints.authenticate;

public class Controller implements Initializable {
    private static final double ITEM_HEIGHT = 68.125;
    public JFXToggleButton separateByCourse;
    public JFXToggleButton removePostfix;
    public JFXButton batchDownload;
    public JFXButton fileDownload;
    public JFXTextField fileName1;
    public JFXTextArea fileDescription;

    public JFXTextArea workRequirement;
    public Hyperlink workRequirementAttachment;
    public JFXTextArea workContent;
    public JFXTextField workAttachment;
    public JFXButton workCommit;
    public JFXTextField workName1;
    public JFXButton workAlert;
    public JFXButton refreshButton;
    public JFXButton openInBrowserButton;

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
    @FXML private JFXTextArea bulletinContent;
    @FXML private JFXTextField bulletinTitle1;
    @FXML private JFXButton bulletinAlertButton;
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
    @FXML private Pane mask;
    @FXML private StackPane spinner;
    @FXML private JFXListView<CourseItem> courseList;
    @FXML private JFXToolbar TopBar;
    @FXML private JFXTextField username;
    @FXML private JFXPasswordField password;
    @FXML private JFXButton login;
    private boolean dirtyUpdateFlag = false;
    private boolean choosingFile = false;

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
                stateSwitch(0, true);
            } else {
                if (nV.getValue() instanceof Bulletin) {
                    Bulletin value = nV.getValue();
                    stateSwitch(0, false);
                    bulletinTitle1.setText(value.name.get());
                    value.resolveContent().thenAccept(stringProperty -> Platform.runLater(() -> bulletinContent.setText(stringProperty.get())));
                } else {
                    stateSwitch(0, true);
                }
            }
        });
        stateSwitch(0, true);

        // file
        fileTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        fileName.setCellValueFactory(p -> fileName.validateValue(p) ? p.getValue().getValue().title : fileName.getComputedValue(p));
        fileDate.setCellValueFactory(p -> fileDate.validateValue(p) ? p.getValue().getValue().uploadTime : fileSize.getComputedValue(p));
        fileSize.setCellValueFactory(p -> fileSize.validateValue(p) ? p.getValue().getValue().size : fileSize.getComputedValue(p));
        fileRead.setCellValueFactory(p -> fileRead.validateValue(p) ? p.getValue().getValue().isRead : fileRead.getComputedValue(p));
        fileTable.getSelectionModel().selectedItemProperty().addListener((o1, oV, nV) -> {
            if (dirtyUpdateFlag) return;
            if (nV == null) {
                stateSwitch(1, true);
            } else {
                if (nV.getValue() instanceof FileEntry) {
                    FileEntry value = nV.getValue();
                    stateSwitch(1, false);
                    fileName1.setText(value.title.get());
                    fileDescription.setText(value.description.get());
                } else {
                    stateSwitch(1, true);
                }
            }
        });
        stateSwitch(1, true);
        separateByCourse.selectedProperty().bindBidirectional(Settings.INSTANCE.separateByCourse);
        removePostfix.selectedProperty().bindBidirectional(Settings.INSTANCE.removePostfix);

        // work
        workName.setCellValueFactory(p -> workName.validateValue(p) ? p.getValue().getValue().title : workName.getComputedValue(p));
        workDue.setCellValueFactory(p -> workDue.validateValue(p) ? p.getValue().getValue().deadline : workDue.getComputedValue(p));
        workDone.setCellValueFactory(p -> workDone.validateValue(p) ? p.getValue().getValue().isHandedIn : workDone.getComputedValue(p));
        workTable.getSelectionModel().selectedItemProperty().addListener((o1, oV, nV) -> {
            if (dirtyUpdateFlag) return;
            if (nV == null) {
                stateSwitch(2, true);
            } else {
                if (nV.getValue() instanceof Operation) {
                    Operation value = nV.getValue();
                    stateSwitch(2, false);
                    workName1.setText(value.title.get());
                    try {
                        workCommit.setDisable(new SimpleDateFormat("yyyy-MM-dd").parse(value.deadline.get()).compareTo(new Date()) < 0);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    value.resolveDetail().thenAccept(aVoid -> Platform.runLater(() -> {
                        workRequirement.setText(value.getDescription());
                        if (value.isAttachmentExists()) {
                            workRequirementAttachment.setText(value.getAttachmentName());
                            workRequirementAttachment.setDisable(false);
                        } else {
                            workRequirementAttachment.setText("");
                            workRequirementAttachment.setDisable(true);
                        }
                    }));
                } else {
                    stateSwitch(2, true);
                }
            }
        });
        stateSwitch(2, true);
        workCommit.setDisable(true);
        workAttachment.setEditable(false);
        workAttachment.focusedProperty().addListener((o, oV, nV) -> {
            if (nV && !choosingFile) {
                choosingFile = true;
                workAttachment.setText("");
                Path file = Util.requestOpenFile(null);
                if (file != null) {
                    workAttachment.setText(file.toString());
                }
                choosingFile = false;
                workCommit.requestFocus();
            }
        });

        JFXScrollPane.smoothScrolling(courseListScrollPane);

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
                // bulletin
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

    @FXML public void addBulletinAlert(ActionEvent actionEvent) {
        Bulletin currentBulletin = bulletinTable.getSelectionModel().getSelectedItem().getValue();
        Util.requestTime(main, date -> Notification.addNotification(currentBulletin.name.get(), currentBulletin.content.get(), date));
    }

    public void addWorkAlert(ActionEvent actionEvent) {
        Operation currentOperation = workTable.getSelectionModel().getSelectedItem().getValue();
        Util.requestTime(main, date -> Notification.addNotification(currentOperation.title.get(), "", date));
    }

    public void download(ActionEvent event) {
        TreeItem<FileEntry> selectedItem = fileTable.getSelectionModel().getSelectedItem();
        FileEntry entry = selectedItem.getValue();
        DownloadManager.enqueue(courseList.getSelectionModel().selectedItemProperty().get(), entry);
    }

    public void batchDownload(ActionEvent event) {
        ObservableList<TreeItem<FileEntry>> selectedItems = fileTable.getSelectionModel().getSelectedItems();
        FileEntry[] entries = new FileEntry[selectedItems.size()];
        for (int i = 0; i < entries.length; i++) {
            entries[i] = selectedItems.get(i).getValue();
        }
        DownloadManager.enqueue(courseList.getSelectionModel().selectedItemProperty().get(), entries);
    }

    public void commit(ActionEvent event) {
        String file = workAttachment.getText();
        String text = workContent.getText();
        Operation operation = workTable.getSelectionModel().getSelectedItem().getValue();
        (file.isEmpty() ? operation.submit(text) : operation.submit(text, new File(file))).thenAccept(aVoid -> Platform.runLater(() -> snackBar.show("提交成功！", "success", 3000)));
    }

    public void attachmentDownload(ActionEvent event) {
        Path path = Util.requestDir(null);
        if (path != null) {
            workTable.getSelectionModel().getSelectedItem().getValue().downloadRequirementAttachment(path);
        }
    }

    private void stateSwitch(int type, boolean state) {
        switch (type) {
            case 0:
                bulletinContent.setText("");
                bulletinTitle1.setText("");
                bulletinAlertButton.setDisable(state);
                break;
            case 1:
                fileDescription.setText("");
                fileName1.setText("");
                fileDownload.setDisable(state);
                batchDownload.setDisable(state);
                break;
            case 2:
                workName1.setText("");
                workRequirement.setText("");
                workRequirementAttachment.setText("");
                workAlert.setDisable(state);
                break;
        }
    }

    public void openBrowser(ActionEvent event) {

    }

    public void refresh(ActionEvent event) {

    }

    public void openProfile(ActionEvent event) {

    }

    public void openInbox(ActionEvent event) {

    }
}
