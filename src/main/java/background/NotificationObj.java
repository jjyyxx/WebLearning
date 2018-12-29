package background;

import java.awt.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationObj implements Serializable {
    /**
     * 序列化的UID标识符
     */
    private static final long serialVersionUID = 75532542535345L;
    /**
     * 计时器，用于触发提醒
     */
    private static final Timer TIMER = new Timer();

    /**
     * 提醒的名称
     */
    public final String title;
    /**
     * 提醒的内容
     */
    public final String content;
    /**
     * 提醒的触发时间
     */
    public final Date date;
    /**
     * 提醒的类型
     */
    public final NotificationType type;
    /**
     * 定时器任务，用于发出提醒
     */
    private transient TimerTask task;

    NotificationObj(String title, String content, Date date, NotificationType type) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.type = type;
        Notification.notifications.add(this);
    }

    /**
     * 在指定时间触发提醒
     */
    void schedule() {
        task = new TimerTask() {
            @Override public void run() {
                Notification.notifications.remove(NotificationObj.this);
                Notification.icon.displayMessage(title, content, TrayIcon.MessageType.INFO);
            }
        };
        TIMER.schedule(task, date);
    }

    /**
     * 取消某个提醒
     */
    public void cancel() {
        task.cancel();
        Notification.notifications.remove(this);
    }
}