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

public class InboxPane extends Pane {
    private static final URL fxml = InboxPane.class.getResource("/app/controls/InboxPane.fxml");
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
        list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public void cancel() {
        if (list.getSelectionModel().isEmpty()) return;
        NotificationItem selectedItem = list.getSelectionModel().getSelectedItem();
        list.getItems().remove(selectedItem);
        selectedItem.notificationObj.cancel();
    }

    public void batch() {
        if (list.getSelectionModel().isEmpty()) return;
        ObservableList<NotificationItem> selectedItems = FXCollections.observableArrayList(list.getSelectionModel().getSelectedItems());
        list.getItems().removeAll(selectedItems);
        for (NotificationItem selectedItem : selectedItems) {
            selectedItem.notificationObj.cancel();
        }

    }
}
