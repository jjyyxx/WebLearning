package background;

import common.DataStore;

import java.awt.*;
import java.io.Serializable;
import java.util.*;
import java.util.List;

public class Notification {
    private static class NotificationObj implements Serializable {
        private static final long serialVersionUID = 75532542535345L;

        private final String title;
        private final String content;
        private final Date date;

        NotificationObj(String title, String content, Date date) {
            this.title = title;
            this.content = content;
            this.date = date;
            notifications.add(this);
        }

        void schedule() {
            new Timer().schedule(new TimerTask() {
                @Override public void run() {
                    notifications.remove(NotificationObj.this);
                    icon.displayMessage(title, content, TrayIcon.MessageType.INFO);
                }
            }, date);
        }
    }

    private static final SystemTray tray;
    private static final TrayIcon icon;
    private static final String TITLE = "网络学堂";
    private static final List<NotificationObj> notifications = Objects.requireNonNullElseGet(DataStore.getObj("notifications"), ArrayList::new);

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
        Runtime.getRuntime().addShutdownHook(new Thread(() -> DataStore.putObj("notifications", notifications)));
    }

    public static void addNotification(String title, String content, Date date) {
        new NotificationObj(title, content, date).schedule();
    }
}
