package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class App extends Application {
    public static Stage stage;
    private static final String TITLE = "网络学堂";
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 600;

    @Override
    public void start(Stage stage) throws Exception{
        App.stage = stage;
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle(TITLE);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("app.fxml"));
        Parent root = loader.load();
        Controller controller = loader.getController();
        controller.setStage(stage);
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(e -> System.exit(0));
    }
}
