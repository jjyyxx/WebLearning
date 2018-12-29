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
    /**
     * 系统任务栏实例
     */
    private static final SystemTray tray;
    /**
     * 本应用对应图标
     */
    public static final TrayIcon icon;
    /**
     * 任务栏图标的名称
     */
    private static final String TITLE = "网络学堂";
    /**
     * 在等待触发的提醒列表
     */
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

    /**
     * 增加新的提醒
     * @param title 提醒的标题
     * @param content 提醒的内容
     * @param date 提醒触发的时间
     * @param type 提醒种类
     */
    public static void addNotification(String title, String content, Date date, NotificationType type) {
        new NotificationObj(title, content, date, type).schedule();
    }
}
