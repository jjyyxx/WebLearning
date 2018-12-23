package app.controls;

import com.jfoenix.controls.JFXToggleButton;
import common.Settings;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.net.URL;

public class ProfilePane extends Pane {
    private static final URL fxml = ProfilePane.class.getResource("/app/controls/SettingPane.fxml");
    public static final ProfilePane INSTANCE = new ProfilePane();
    public JFXToggleButton autologin;
    public JFXToggleButton autostart;
    public JFXToggleButton separateByCourse;

    private ProfilePane() {
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
}
