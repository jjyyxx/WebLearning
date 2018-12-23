package background;

import java.awt.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationObj implements Serializable {
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
        Notification.notifications.add(this);
    }

    void schedule() {
        task = new TimerTask() {
            @Override public void run() {
                Notification.notifications.remove(NotificationObj.this);
                Notification.icon.displayMessage(title, content, TrayIcon.MessageType.INFO);
            }
        };
        TIMER.schedule(task, date);
    }

    public void cancel() {
        task.cancel();
        Notification.notifications.remove(this);
    }
}