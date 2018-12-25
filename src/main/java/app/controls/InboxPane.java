package app.controls;

import background.Notification;
import background.NotificationObj;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Locale;

/**
 * 集中各类用户提醒的弹出框
 */
public class InboxPane extends Pane {
    private static final URL fxml = InboxPane.class.getResource("/app/controls/InboxPane.fxml");
    // 因为在整个生命周期中是唯一的，使用单例以提升性能
    public static final InboxPane INSTANCE = new InboxPane();

    public JFXListView<NotificationItem> list;
    public JFXTextField name;
    public JFXTextArea content;
    public JFXTextField date;

    private InboxPane() {
        FXMLLoader fxmlLoader = new FXMLLoader(fxml);
        fxmlLoader.setRoot(this);
        fxmlLoader.setControllerFactory(ignored -> this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        // 监听列表中元素的变化作出响应
        Notification.notifications.addListener((ListChangeListener<? super NotificationObj>) change -> {
            ObservableList<NotificationItem> notificationItems = FXCollections.observableArrayList();
            for (NotificationObj notificationObj : change.getList()) {
                notificationItems.add(new NotificationItem(notificationObj));
            }
            Platform.runLater(() -> list.setItems(notificationItems));
        });
        ObservableList<NotificationItem> notificationItems = FXCollections.observableArrayList();
        for (NotificationObj notificationObj : Notification.notifications) {
            notificationItems.add(new NotificationItem(notificationObj));
        }
        list.setItems(notificationItems);
        // 右侧详细信息的显示与隐藏
        list.getSelectionModel().selectedItemProperty().addListener((v, o, n) -> {
            if (n != null) {
                name.setText(n.notificationObj.title);
                content.setText(n.notificationObj.content);
                date.setText(DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT, Locale.CHINESE).format(n.notificationObj.date));
            } else {
                name.setText("");
                content.setText("");
                date.setText("");
            }
        });
        // 允许多选
        list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    /**
     * UI中取消按钮的对应处理程序
     */
    public void cancel() {
        if (list.getSelectionModel().isEmpty()) return;
        NotificationItem selectedItem = list.getSelectionModel().getSelectedItem();
        list.getItems().remove(selectedItem);
        selectedItem.notificationObj.cancel();
    }

    /**
     * UI中批量取消按钮的对应处理程序
     */
    public void batch() {
        if (list.getSelectionModel().isEmpty()) return;
        ObservableList<NotificationItem> selectedItems = FXCollections.observableArrayList(list.getSelectionModel().getSelectedItems());
        list.getItems().removeAll(selectedItems);
        for (NotificationItem selectedItem : selectedItems) {
            selectedItem.notificationObj.cancel();
        }
    }
}
