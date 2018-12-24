package app.controls;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXToggleButton;
import common.DataStore;
import common.Settings;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

public class SettingPane extends Pane {
    private static final URL fxml = SettingPane.class.getResource("/app/controls/SettingPane.fxml");
    public static final SettingPane INSTANCE = new SettingPane();
    public JFXToggleButton autologin;
    public JFXToggleButton autostart;
    public JFXToggleButton separateByCourse;
    public JFXButton exit;

    private SettingPane() {
        FXMLLoader fxmlLoader = new FXMLLoader(fxml);
        fxmlLoader.setRoot(this);
        fxmlLoader.setControllerFactory(ignored -> this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        autologin.setSelected(Settings.INSTANCE.autologin.get());
        autostart.setSelected(Settings.INSTANCE.autostart.get());
        Settings.INSTANCE.autologin.bind(autologin.selectedProperty());
        Settings.INSTANCE.autostart.bind(autostart.selectedProperty());
        separateByCourse.selectedProperty().bindBidirectional(Settings.INSTANCE.separateByCourse);
    }

    public void logout() {
        DataStore.put("username", "");
        DataStore.putEncrypt("password", "");
        Platform.exit();
        System.exit(0);
    }

    public void about() {
        try {
            Desktop.getDesktop().browse(URI.create("https://github.com/jjyyxx/WebLearning"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void issue() {
        try {
            Desktop.getDesktop().browse(URI.create("https://github.com/jjyyxx/WebLearning/issues"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setName(String name) {
        exit.setText("退出当前账户：" + name);
    }
}
