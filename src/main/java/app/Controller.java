package app;

import app.controls.CourseItem;
import app.controls.InboxPane;
import app.controls.SettingPane;
import background.DownloadManager;
import background.Notification;
import background.NotificationType;
import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import common.AuthException;
import common.DataStore;
import common.Navigable;
import common.Settings;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import platform.win.Imm32;
import weblearning.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

/**
 * 主要的UI控制器。本项目采用了类似MVC的架构，用Controller处理用户逻辑，fxml中定义UI界面，weblearning的相关类中提供数据模型
 */
public class Controller implements Initializable {
    // 单个列表项的高度，用于计算ScrollPane的高度
    private static final double ITEM_HEIGHT = 68.125;

    // 页面上UI组件的引用
    // 课程文件部分
    @FXML private JFXToggleButton separateByCourse;
    @FXML private JFXButton fileOpen;
    @FXML private JFXButton batchDownload;
    @FXML private JFXButton fileDownload;
    @FXML private JFXTextField fileName1;
    @FXML private JFXTextArea fileDescription;

    // 课程作业部分
    @FXML private JFXTextArea workRequirement;
    @FXML private Hyperlink workRequirementAttachment;
    @FXML private JFXTextArea workContent;
    @FXML private JFXTextField workAttachment;
    @FXML private JFXButton workCommit;
    @FXML private JFXTextField workName1;
    @FXML private JFXButton workAlert;

    // 课程公告部分
    @FXML private JFXTextArea bulletinContent;
    @FXML private JFXTextField bulletinTitle1;
    @FXML private JFXButton bulletinAlertButton;

    // 课程作业部分的table
    @FXML private JFXTreeTableView<Operation> workTable;
    @FXML private JFXTreeTableColumn<Operation, String> workName;
    @FXML private JFXTreeTableColumn<Operation, String> workDue;
    @FXML private JFXTreeTableColumn<Operation, String> workDone;

    // 课程文件部分的table
    @FXML private JFXTreeTableView<FileEntry> fileTable;
    @FXML private JFXTreeTableColumn<FileEntry, String> fileName;
    @FXML private JFXTreeTableColumn<FileEntry, String> fileDate;
    @FXML private JFXTreeTableColumn<FileEntry, String> fileSize;
    @FXML private JFXTreeTableColumn<FileEntry, String> fileRead;

    // 课程公告部分的table
    @FXML private JFXTreeTableView<Bulletin> bulletinTable;
    @FXML private JFXTreeTableColumn<Bulletin, String> bulletinTitle;
    @FXML private JFXTreeTableColumn<Bulletin, String> bulletinDate;
    @FXML private JFXTreeTableColumn<Bulletin, String> bulletinRead;

    @FXML private JFXTabPane mainTabs; // 主TabPane
    @FXML private ScrollPane courseListScrollPane; // 课程列表的容器
    @FXML private JFXListView<CourseItem> courseList; // 课程列表
    @FXML private Pane root; // 根容器
    @FXML private StackPane main; // 主容器，处理页面间的切换
    @FXML private Pane coursePane; // 登录后的页面
    @FXML private Pane mask; // 遮罩层
    @FXML private StackPane spinner; // 加载提示
    @FXML private JFXTextField username; // 用户名输入框
    @FXML private JFXPasswordField password; // 密码输入框

    private final JFXDialog dialog = new JFXDialog(); // 用于容纳Setting和Inbox的对话框
    private JFXTreeTableView currentTable; // 记录当前的Table的变量

    // 非UI变量
    private final List<Path> tempPaths = new ArrayList<>(); // 生成的临时文件的路径列表，用于退出时清除
    private boolean dirtyBulletinUpdateFlag = false; // 防止UI更新过程中发生循环操作的标志变量，作用于公告表格
    private boolean dirtyFileUpdateFlag = false; // 防止UI更新过程中发生循环操作的标志变量，作用于文件表格
    private boolean dirtyOperationUpdateFlag = false; // 防止UI更新过程中发生循环操作的标志变量，作用于作业表格
    private boolean choosingFile = false; // 防止由于重新获得焦点而反复触发选择文件的标志变量
    private double sceneX; // 当前窗口位置的横坐标，用于计算拖动
    private double sceneY; // 当前窗口位置的纵坐标，用于计算拖动
    private Stage stage; // 对窗口本身的引用

    /**
     * 初始化UI组件的函数，此时上方所有的UI组件变量已被赋值
     */
    @Override public void initialize(URL location, ResourceBundle resources) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                for (Path tempPath : tempPaths) {
                    Files.deleteIfExists(tempPath);
                }
            } catch (IOException ignored) {}
        }));

        // 预加载，防止Listener由于JIT的懒加载特性出现工作异常
        try {
            Class.forName("app.controls.InboxPane");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Util.initSnackBar(root);

        ObservableList<Node> children = main.getChildren();
        children.forEach(node -> node.setVisible(false));
        children.get(children.size() - 1).setVisible(true);

        mainTabs.getSelectionModel().selectedIndexProperty().addListener((o, oV, nV) -> {
            switch (nV.intValue()) {
                case 0:
                    currentTable = bulletinTable;
                    break;
                case 1:
                    currentTable = fileTable;
                    break;
                case 2:
                    currentTable = workTable;
                    break;
            }
        });
        currentTable = bulletinTable;

        // 公告页面的初始化
        bulletinRead.setCellValueFactory(p -> bulletinRead.validateValue(p) ? p.getValue().getValue().isRead : bulletinRead.getComputedValue(p));
        bulletinTitle.setCellValueFactory(p -> bulletinTitle.validateValue(p) ? p.getValue().getValue().name : bulletinTitle.getComputedValue(p));
        bulletinDate.setCellValueFactory(p -> bulletinDate.validateValue(p) ? p.getValue().getValue().time : bulletinDate.getComputedValue(p));
        bulletinTable.getSelectionModel().selectedItemProperty().addListener((o1, oV, nV) -> {
            if (dirtyBulletinUpdateFlag) return;
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
        bulletinTable.setShowRoot(false);
        bulletinTable.setEditable(false);

        // 文件页面的初始化
        fileTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        fileName.setCellValueFactory(p -> fileName.validateValue(p) ? p.getValue().getValue().title : fileName.getComputedValue(p));
        fileDate.setCellValueFactory(p -> fileDate.validateValue(p) ? p.getValue().getValue().uploadTime : fileSize.getComputedValue(p));
        fileSize.setCellValueFactory(p -> fileSize.validateValue(p) ? p.getValue().getValue().size : fileSize.getComputedValue(p));
        fileRead.setCellValueFactory(p -> fileRead.validateValue(p) ? p.getValue().getValue().isRead : fileRead.getComputedValue(p));
        fileTable.getSelectionModel().selectedItemProperty().addListener((o1, oV, nV) -> {
            if (dirtyFileUpdateFlag) return;
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
        fileTable.setShowRoot(false);
        fileTable.setEditable(false);

        // 作业页面的初始化
        workName.setCellValueFactory(p -> workName.validateValue(p) ? p.getValue().getValue().title : workName.getComputedValue(p));
        workDue.setCellValueFactory(p -> workDue.validateValue(p) ? p.getValue().getValue().deadline : workDue.getComputedValue(p));
        workDone.setCellValueFactory(p -> workDone.validateValue(p) ? p.getValue().getValue().isHandedIn : workDone.getComputedValue(p));
        workTable.getSelectionModel().selectedItemProperty().addListener((o1, oV, nV) -> {
            if (dirtyOperationUpdateFlag) return;
            if (nV == null) {
                stateSwitch(2, true);
            } else {
                if (nV.getValue() instanceof Operation) {
                    Operation value = nV.getValue();
                    stateSwitch(2, false);
                    workName1.setText(value.title.get());
                    try {
                        workCommit.setDisable(new SimpleDateFormat("yyyy-MM-dd").parse(value.deadline.get()).getTime()+86400000l<(new Date().getTime()));
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
        workTable.setShowRoot(false);
        workTable.setEditable(false);

        // 课程列表的平滑滚动
        JFXScrollPane.smoothScrolling(courseListScrollPane);

        // 防止输入密码时中文输入法开启
        password.focusedProperty().addListener((observable, oldValue, newValue) -> Imm32.set(newValue));

        // 如果自动登录选项启用，则尝试自动登录
        if (Settings.INSTANCE.autologin.get()) {
            weblearning.Endpoints.authenticate(
                    DataStore.get("username", ""),
                    DataStore.getDecrypt("password", "")
            ).thenAccept(ignored -> afterLogin()).exceptionally(e -> null);
        }
    }

    /**
     * 登录成功后执行的函数，进一步初始化UI组件
     */
    private void afterLogin() {
        SettingPane.INSTANCE.setName(DataStore.get("username", ""));
        Endpoints.getCurriculum().thenAccept(courseData -> Platform.runLater(() -> {
            toggleSpinner(false);
            courseList.setPrefHeight(courseData.size() * ITEM_HEIGHT);
            ObservableList<CourseItem> items = courseList.getItems();
            for (CourseData each : courseData.values()) {
                items.add(new CourseItem(each));
            }
            courseList.getSelectionModel().selectedItemProperty().addListener((o, oldValue, newValue) -> refreshContent(newValue));
            courseList.getSelectionModel().selectFirst();
        })).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });

        Platform.runLater(() -> {
            Util.showSnackBar("登录成功！", 3000, "success");
            switchPane(coursePane);
        });
    }

    /**
     * 选择某一课程之后刷新内容的函数
     * @param item 被新选中的课程
     */
    private void refreshContent(CourseItem item) {
        // 公告刷新
        item.courseData.resolveBulletins().thenAccept(bulletins -> Platform.runLater(() -> {
            dirtyBulletinUpdateFlag = true;
            bulletinTable.unGroup(bulletinRead);
            ObservableList<Bulletin> bulletinList = FXCollections.observableArrayList(Arrays.asList(bulletins));
            final TreeItem<Bulletin> root = new RecursiveTreeItem<>(bulletinList, RecursiveTreeObject::getChildren);
            bulletinTable.setRoot(root);
            bulletinTable.group(bulletinRead);
            for (TreeItem<Bulletin> child : bulletinTable.getRoot().getChildren()) {
                child.setExpanded(true);
            }
            dirtyBulletinUpdateFlag = false;
            bulletinTable.getSelectionModel().clearSelection();
        }));

        // 文件刷新
        item.courseData.resolveFileEntries().thenAccept(stringMap -> Platform.runLater(() -> {
            dirtyFileUpdateFlag = true;
            fileTable.unGroup(fileRead);
            List<FileEntry> fileEntries = new ArrayList<>();
            for (Map.Entry<String, FileEntry[]> value : stringMap.entrySet()) {
                fileEntries.addAll(Arrays.asList(value.getValue()));
            }
            ObservableList<FileEntry> fileList = FXCollections.observableArrayList(fileEntries);
            final TreeItem<FileEntry> root = new RecursiveTreeItem<>(fileList, RecursiveTreeObject::getChildren);
            fileTable.setRoot(root);

            fileTable.group(fileRead);
            for (TreeItem<FileEntry> child : fileTable.getRoot().getChildren()) {
                child.setExpanded(true);
            }
            fileTable.getSortOrder().clear();
            fileTable.getSortOrder().add(fileRead);
            fileRead.setSortType(TreeTableColumn.SortType.ASCENDING);
            fileRead.setSortable(true);
            dirtyFileUpdateFlag = false;
            fileTable.getSelectionModel().clearSelection();
        }));

        // 作业刷新
        item.courseData.resolveOperations().thenAccept(operations -> Platform.runLater(() -> {
            dirtyOperationUpdateFlag = true;
            workTable.unGroup(workDone);
            ObservableList<Operation> operationList = FXCollections.observableArrayList(Arrays.asList(operations));
            final TreeItem<Operation> root = new RecursiveTreeItem<>(operationList, RecursiveTreeObject::getChildren);
            workTable.setRoot(root);
            workTable.group(workDone);
            for (TreeItem<Operation> child : workTable.getRoot().getChildren()) {
                child.setExpanded(true);
            }
            workTable.getSortOrder().clear();
            workTable.getSortOrder().add(workDone);
            workDone.setSortType(TreeTableColumn.SortType.ASCENDING);
            workDone.setSortable(true);
            dirtyOperationUpdateFlag = false;
            workTable.getSelectionModel().clearSelection();
        }));
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

    /**
     * 文件下载的三种模式的抽象
     */
    private void download(boolean batch, boolean open) {
        FileEntry[] entries;
        if (batch) {
            ObservableList<TreeItem<FileEntry>> selectedItems = fileTable.getSelectionModel().getSelectedItems();
            FilteredList<TreeItem<FileEntry>> filtered = selectedItems.filtered(fileEntryTreeItem -> fileEntryTreeItem.getValue() instanceof FileEntry);
            if (filtered.size() == 0) return;
            entries = new FileEntry[filtered.size()];
            for (int i = 0; i < filtered.size(); i++) {
                entries[i] = filtered.get(i).getValue();
            }
        } else {
            TreeItem<FileEntry> selectedItem = fileTable.getSelectionModel().getSelectedItem();
            if (!(selectedItem.getValue() instanceof FileEntry)) return;
            entries = new FileEntry[]{ selectedItem.getValue() };
        }
        CourseData courseData = courseList.getSelectionModel().selectedItemProperty().get().courseData;
        DownloadManager.enqueue(courseData, entries, open);
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
                fileOpen.setDisable(state);
                break;
            case 2:
                workName1.setText("");
                workRequirement.setText("");
                workRequirementAttachment.setText("");
                workAlert.setDisable(state);
                break;
        }
    }

    // 以下均为UI操作的响应函数

    /**
     * 添加公告提醒
     */
    @FXML private void addBulletinAlert() {
        Bulletin currentBulletin = bulletinTable.getSelectionModel().getSelectedItem().getValue();
        Util.requestTime(main, date -> Notification.addNotification(currentBulletin.name.get(), currentBulletin.content.get(), date, NotificationType.ANNOUNCEMENT));
    }

    /**
     * 添加作业提醒
     */
    @FXML private void addWorkAlert() {
        Operation currentOperation = workTable.getSelectionModel().getSelectedItem().getValue();
        Util.requestTime(main, date -> Notification.addNotification(currentOperation.title.get(), "", date, NotificationType.ASSIGNMENT));
    }

    /**
     * 文件下载
     */
    @FXML private void fileDownload() {
        download(false, false);
    }

    /**
     * 文件批量下载
     */
    @FXML private void batchDownload() {
        download(true, false);
    }

    /**
     * 提交作业
     */
    @FXML private void commit() {
        String file = workAttachment.getText();
        String text = workContent.getText();
        Operation operation = workTable.getSelectionModel().getSelectedItem().getValue();
        (file.isEmpty() ? operation.submit(text) : operation.submit(text, new File(file)))
                .thenAccept(aVoid -> Platform.runLater(() -> Util.showSnackBar("提交成功！", 3000, "success")))
                .exceptionally(throwable -> {
                    Util.showSnackBar("提交失败！", 3000, "error");
                    return null;
                });
    }

    /**
     * 文件打开
     */
    @FXML private void fileOpen() {
        download(false, true);
    }

    /**
     * 下载作业要求附件
     */
    @FXML private void attachmentDownload() {
        CourseData courseData = courseList.getSelectionModel().selectedItemProperty().get().courseData;
        DownloadManager.enqueue(courseData, workTable.getSelectionModel().getSelectedItem().getValue());
    }

    /**
     * 刷新当前课程内容
     */
    @FXML private void refresh() {
        refreshContent(courseList.getSelectionModel().getSelectedItem());
    }

    /**
     * 为在浏览器中打开设计的跳转页
     */
    private static final String TEMPLATE = "<!doctypehtml><html lang=\"zh\"><meta charset=\"UTF-8\"><title>中转页</title><body><script>fetch(\"https://learn.tsinghua.edu.cn/MultiLanguage/lesson/teacher/loginteacher.jsp\",{credentials:\"include\",headers:{\"content-type\":\"application/x-www-form-urlencoded\"},body:\"userid=jyx17&userpass=DICKdiao123\",method:\"POST\",mode:\"no-cors\"}).then(()=>location.replace(\"URL\"))</script>";
    @FXML private void openBrowser() {
        try {
            String url = ((TreeItem<Navigable>) currentTable.getSelectionModel().getSelectedItem()).getValue().getURL().toString();
            String content = TEMPLATE.replace("NAME", DataStore.get("username", ""))
                    .replace("PASS", DataStore.getDecrypt("password", ""))
                    .replace("URL", url);
            Path tempFile = Files.createTempFile(null, ".html");
            tempPaths.add(tempFile);
            Files.write(tempFile, content.getBytes(StandardCharsets.UTF_8));
            Desktop.getDesktop().browse(tempFile.toUri());
        } catch (IOException e) {
            Util.showSnackBar("无法打开浏览器！", 3000, "error");
        } catch (NullPointerException | ClassCastException ignored) {}
    }

    /**
     * 打开设置对话框
     */
    @FXML private void openSetting() {
        dialog.setContent(SettingPane.INSTANCE);
        dialog.show(main);
    }

    /**
     * 添加通知提醒
     */
    @FXML private void openInbox() {
        dialog.setContent(InboxPane.INSTANCE);
        dialog.show(main);
    }

    /**
     * 关闭
     */
    @FXML private void close() {
        stage.close();
        System.exit(0);
    }

    /**
     * 最小化
     */
    @FXML private void minimize() {
        stage.setIconified(true);
    }

    /**
     * 登录
     */
    @FXML private void login() {
        String name = username.getText();
        String pass = password.getText();
        toggleSpinner(true);
        Endpoints.authenticate(name, pass)
                .thenAccept(ignored -> {
                    DataStore.put("username", name);
                    DataStore.putEncrypt("password", pass);
                    afterLogin();
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        toggleSpinner(false);
                        String text = e instanceof AuthException ? "用户名或密码不正确，请检查。" : "网络连接不可用，请检查。";
                        Util.showSnackBar(text, 3000, "error");
                    });
                    return null;
                });
    }

    /**
     * 顶部拖动
     */
    @FXML private void topBarDragging(MouseEvent event) {
        stage.setX(event.getScreenX() - sceneX);
        stage.setY(event.getScreenY() - sceneY);
    }

    /**
     * 顶部按下，和上面的方法相互配合
     */
    @FXML private void topBarPressed(MouseEvent event) {
        sceneX = event.getSceneX();
        sceneY = event.getSceneY();
    }
}
