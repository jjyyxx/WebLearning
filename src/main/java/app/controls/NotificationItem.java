package app.controls;

import background.Notification;
import de.jensd.fx.glyphs.materialicons.MaterialIconView;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.net.URL;

public class NotificationItem extends Pane {
    private static final URL fxml = CourseItem.class.getResource("/app/controls/NotificationItem.fxml");
    @FXML private MaterialIconView icon;
    @FXML private Label name;
    public final Notification.NotificationObj notificationObj;

    public NotificationItem(Notification.NotificationObj notificationObj) {
        this.notificationObj = notificationObj;
        FXMLLoader fxmlLoader = new FXMLLoader(fxml);
        fxmlLoader.setRoot(this);
        fxmlLoader.setControllerFactory(ignored -> this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        name.setText(notificationObj.title);
        icon.setGlyphName(notificationObj.type.name());
    }
}
