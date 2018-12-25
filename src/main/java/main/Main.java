package main;

import javafx.application.Application;

/**
 * 程序入口，预加载Endpoints与Notification
 */
public class Main {
    public static void main(String[] args) {
        try {
            Class.forName("weblearning.Endpoints");
            Class.forName("background.Notification");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        // 启动UI
        Application.launch(app.App.class);
    }
}
