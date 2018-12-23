package background;

import common.DataStore;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.awt.*;
import java.io.Serializable;
import java.util.*;
import java.util.function.Supplier;

public class Notification {
    public static class NotificationObj implements Serializable {
        private static final long serialVersionUID = 75532542535345L;
        private static final Timer TIMER = new Timer();

        public final String title;
        public final String content;
        public final Date date;
        public final NotificationType type;
        private transient TimerTask task;

        NotificationObj(String title, String content, Date date, NotificationType type) {
            this.title = title;
            this.content = content;
            this.date = date;
            this.type = type;
            notifications.add(this);
        }

        void schedule() {
            task = new TimerTask() {
                @Override public void run() {
                    notifications.remove(NotificationObj.this);
                    icon.displayMessage(title, content, TrayIcon.MessageType.INFO);
                }
            };
            TIMER.schedule(task, date);
        }

        public void cancel() {
            task.cancel();
            notifications.remove(this);
        }
    }

    private static final SystemTray tray;
    private static final TrayIcon icon;
    private static final String TITLE = "网络学堂";
    public static final ObservableList<NotificationObj> notifications = FXCollections.observableArrayList(Objects.requireNonNullElseGet(DataStore.getObj("notifications"), (Supplier<ArrayList<NotificationObj>>) ArrayList::new));

    static {
        tray = SystemTray.getSystemTray();
        icon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(Notification.class.getResource("/main.png")));
        icon.setImageAutoSize(true);
        icon.setToolTip(TITLE);
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
