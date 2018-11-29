package app.controls;

import com.jfoenix.controls.JFXBadge;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import weblearning.CourseData;

import java.io.IOException;
import java.net.URL;

public class CourseItem extends Pane {
    private static final URL fxml = QuickButtonList.class.getResource("/app/controls/CourseItem.fxml");
    @FXML private Label name;
    @FXML private JFXBadge announcement;
    @FXML private JFXBadge file;
    @FXML private JFXBadge assignment;

    public CourseItem(CourseData courseData) {
        FXMLLoader fxmlLoader = new FXMLLoader(fxml);
        fxmlLoader.setRoot(this);
        fxmlLoader.setControllerFactory(ignored -> this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        initialize(courseData);
    }

    private void initialize(CourseData courseData) {
        name.setText(courseData.getName());

        setBadgeOrHide(announcement, courseData.getUnreadBulletins());
        setBadgeOrHide(file, courseData.getUnreadFiles());
        setBadgeOrHide(assignment, courseData.getUnsubmittedOperations());
    }

    private void setBadgeOrHide(JFXBadge badge, int value) {
        if (value == 0) {
            badge.getStyleClass().add("hide");
        } else {
            badge.setText(String.valueOf(value));
        }
    }
}
