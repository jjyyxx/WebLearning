package background;

import app.App;
import common.DataStore;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.function.Supplier;

public class Notification {
    private static final SystemTray tray;
    public static final TrayIcon icon;
    private static final String TITLE = "网络学堂";
    public static final ObservableList<NotificationObj> notifications = FXCollections.observableArrayList(Objects.requireNonNullElseGet(DataStore.getObj("notifications"), (Supplier<ArrayList<NotificationObj>>) ArrayList::new));

    static {
        tray = SystemTray.getSystemTray();
        icon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(Notification.class.getResource("/main.png")));
        final PopupMenu popup = new PopupMenu();
        MenuItem quit = new MenuItem("Exit");
        quit.addActionListener(e -> System.exit(0));
        popup.add(quit);
        icon.setPopupMenu(popup);
        icon.setImageAutoSize(true);
        icon.setToolTip(TITLE);
        icon.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                Platform.runLater(() -> {
                    if (App.stage.isShowing()) {
                        App.stage.hide();
                    } else {
                        App.stage.show();
                    }
                });
            }
        });
        try {
            tray.add(icon);
        } catch (AWTException e) {
            e.printStackTrace();
        }

        notifications.forEach(NotificationObj::schedule);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> DataStore.putObj("notifications", new ArrayList<>(notifications))));
    }

    public static void addNotification(String title, String content, Date date, NotificationType type) {
        new NotificationObj(title, content, date, type).schedule();
    }
}
