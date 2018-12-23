package app.controls;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import weblearning.Endpoints;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

public class ProfilePane extends Pane {
    private static final URL fxml = ProfilePane.class.getResource("/app/controls/ProfilePane.fxml");
    public static final ProfilePane INSTANCE = new ProfilePane();

    static {
        Endpoints.getProfile().thenAccept(INSTANCE::setData);
    }

    private Map<String, String> profile;
    private void setData(Map<String, String> profile) {
        this.profile = profile;
        refresh();
    }

    private void refresh() {

    }

    private ProfilePane() {
        FXMLLoader fxmlLoader = new FXMLLoader(fxml);
        fxmlLoader.setRoot(this);
        fxmlLoader.setControllerFactory(ignored -> this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
