package main;

import javafx.application.Application;

public class Main {
    public static void main(String[] args) {
        try {
            Class.forName("weblearning.Endpoints");
            Class.forName("background.Notification");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Application.launch(app.App.class);
    }
}
