import app.App;
import javafx.application.Platform;

public class Main {
    public static void main(String[] args) {
        try {
            Class.forName("weblearning.Endpoints");
            Class.forName("background.Notification");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Platform.startup(App::launch);
    }
}
