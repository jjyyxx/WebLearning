package app.controls;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXToggleButton;
import common.Settings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.net.URL;

public class SettingPane extends Pane {
    private static final URL fxml = SettingPane.class.getResource("/app/controls/SettingPane.fxml");
    public JFXToggleButton autologin;
    public JFXToggleButton autostart;
    public JFXButton closeButton;
    public JFXToggleButton separateByCourse;
    public JFXToggleButton removePostfix;
    // TODO: associate it with preference

    public SettingPane(EventHandler<ActionEvent> closeHandler) {
        FXMLLoader fxmlLoader = new FXMLLoader(fxml);
        fxmlLoader.setRoot(this);
        fxmlLoader.setControllerFactory(ignored -> this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        closeButton.setOnAction(closeHandler);
        autologin.setSelected(Settings.INSTANCE.autologin.get());
        autostart.setSelected(Settings.INSTANCE.autostart.get());
        Settings.INSTANCE.autologin.bind(autologin.selectedProperty());
        Settings.INSTANCE.autostart.bind(autostart.selectedProperty());
        separateByCourse.selectedProperty().bindBidirectional(Settings.INSTANCE.separateByCourse);
        removePostfix.selectedProperty().bindBidirectional(Settings.INSTANCE.removePostfix);
    }
}
