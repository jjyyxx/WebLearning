package background;

import java.awt.*;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Notification {
    private static final SystemTray tray;
    private static final TrayIcon icon;
    private static final String TITLE = "网络学堂";

    static {
        tray = SystemTray.getSystemTray();
        icon = new TrayIcon(Toolkit.getDefaultToolkit().getImage("C:\\Users\\hjzsj\\Pictures\\QQ浏览器截图\\QQ浏览器截图20171222201303.png"));
        icon.setToolTip(TITLE);
        try {
            tray.add(icon);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public static void addNotification(String title, String content, Date date) {
        new Timer().schedule(new TimerTask() {
            @Override public void run() {
                icon.displayMessage(title, content, TrayIcon.MessageType.INFO);
            }
        }, date);
    }
}
