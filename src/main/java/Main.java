import app.App;
import javafx.application.Platform;

public class Main {
    public static void main(String[] args) {
        Platform.startup(App::launch);
    }
}
