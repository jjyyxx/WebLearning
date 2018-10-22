package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception{
        stage.initStyle(StageStyle.UNDECORATED);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("app.fxml"));
        Parent root = loader.load();
        Controller controller = loader.getController();
        controller.setStage(stage);
        stage.setTitle("网络学堂");
        Scene scene = new Scene(root, 1000, 600);
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(e -> System.exit(0));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
