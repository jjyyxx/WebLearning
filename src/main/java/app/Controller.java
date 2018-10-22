package app;

import Common.AuthException;
import Common.DataStore;
import WebLearning.CourseData;
import WebLearning.Endpoints;
import app.controls.QuickButtonList;
import com.jfoenix.controls.*;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import platform.win.Imm;

import java.net.URL;
import java.util.ResourceBundle;

import static WebLearning.Endpoints.authenticate;

public class Controller implements Initializable {
    private double sceneX;
    private double sceneY;
    private Stage stage;

    @FXML
    private Pane root;
    @FXML
    private StackPane main;
    @FXML
    private Pane loginPane;
    @FXML
    private Pane coursePane;

    @FXML
    public QuickButtonList nodesList;

    @FXML
    public Pane mask;
    @FXML
    public StackPane spinner;

    @FXML
    private JFXListView<Label> courseList;

    @FXML
    private JFXToolbar TopBar;

    @FXML
    private JFXButton closeButton;

    @FXML
    private JFXButton minimizeButton;

    @FXML
    private JFXTextField username;

    @FXML
    private JFXPasswordField password;

    @FXML
    private JFXButton login;

    private JFXSnackbar snackBar;
    private WinDef.HWND hwnd;
    private Pointer himc;

    @FXML
    void close(ActionEvent event) {
        stage.close();
        System.exit(0);
    }

    @FXML
    void minimize(ActionEvent event) {
        stage.setIconified(true);
    }

    @FXML
    void login(ActionEvent event) {
        String name = username.getText();
        String pass = password.getText();
        toggleSpinner(true);
        authenticate(name, pass)
                .thenAccept(ignored -> {
                    DataStore.put("username", name);
                    DataStore.put("password", pass);
                    Platform.runLater(() -> {
                        snackBar.show("登录成功！", "success", 3000);
                        afterLogin();
                    });
                })
                .exceptionally(e -> {
                    toggleSpinner(false);
                    String text = e instanceof AuthException ? "用户名或密码不正确，请检查。" : "网络连接不可用，请检查。";
                    snackBar.show(text, "error", 3000);
                    return null;
                });
    }

    @FXML
    void topBarDragging(MouseEvent event) {
        stage.setX(event.getScreenX() - sceneX);
        stage.setY(event.getScreenY() - sceneY);
    }

    @FXML
    void topBarPressed(MouseEvent event) {
        sceneX = event.getSceneX();
        sceneY = event.getSceneY();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        snackBar = new JFXSnackbar(root);
        ObservableList<Node> children = main.getChildren();
        children.forEach(node -> node.setVisible(false));
        children.get(children.size() - 1).setVisible(true);
        try {
            Class.forName("WebLearning.Endpoints");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        password.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                if (hwnd == null) {
                    hwnd = User32.INSTANCE.FindWindow(null, "网络学堂");
                }
                himc = Imm.ImmAssociateContext(hwnd, null);
            } else {
                Imm.ImmAssociateContext(hwnd, himc);
            }
        });
        Pointer b = Imm.ImmAssociateContext(hwnd, null);
//        WebLearning.Endpoints.authenticate(DataStore.get("username", ""), DataStore.get("password", ""))
//                .thenAccept(ignored -> afterLogin())
//                .exceptionally(e -> {
//                    e.printStackTrace();
//                    return null;
//                });
    }

    void afterLogin() {
        switchPane(coursePane);

        nodesList.setVisible(true);

        Endpoints.getCurriculum().thenAccept(courseData -> {
            toggleSpinner(false);
            for (CourseData each : courseData) {
                courseList.getItems().add(new Label(each.getName()));
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    void toggleSpinner(boolean show) {
        mask.setVisible(show);
        spinner.setVisible(show);
    }

    void switchPane(Pane nextPane) {
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
}
