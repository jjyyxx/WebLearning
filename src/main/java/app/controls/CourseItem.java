package app.controls;

import com.jfoenix.controls.JFXBadge;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import weblearning.CourseData;

import java.io.IOException;
import java.net.URL;

/**
 * 自定义UI部件，课程列表的单个课程
 */
public class CourseItem extends Pane {
    private static final URL fxml = CourseItem.class.getResource("/app/controls/CourseItem.fxml");
    @FXML private Label name;
    @FXML private JFXBadge announcement;
    @FXML private JFXBadge file;
    @FXML private JFXBadge assignment;
    public final CourseData courseData;

    public CourseItem(CourseData courseData) {
        this.courseData = courseData;
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
        setBadgeOrHide(announcement, courseData.unreadBulletins.intValue());
        setBadgeOrHide(file, courseData.unreadFiles.intValue());
        setBadgeOrHide(assignment, courseData.unsubmittedOperations.intValue());
        // 监听变化，进行UI响应
        courseData.unreadBulletins.addListener((o, oV, nV) -> Platform.runLater(() -> setBadgeOrHide(announcement, nV.intValue())));
        courseData.unreadFiles.addListener((o, oV, nV) -> Platform.runLater(() -> setBadgeOrHide(file, nV.intValue())));
        courseData.unsubmittedOperations.addListener((o, oV, nV) -> Platform.runLater(() -> setBadgeOrHide(assignment, nV.intValue())));
    }

    /**
     * 调整badge的状态，value等于0时隐藏，大于0时出现
     */
    private void setBadgeOrHide(JFXBadge badge, int value) {
        if (value == 0) {
            badge.getStyleClass().add("hide");
        } else {
            badge.setText(String.valueOf(value));
        }
    }
}
