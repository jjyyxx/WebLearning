package app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * 按照JavaFX的要求必须存在的启动类
 * @see javafx.application.Application
 */
public class App extends Application {
    /**
     * 主窗口
     */
    public static Stage stage;
    /**
     * 显示在taskbar中的标题
     */
    private static final String TITLE = "网络学堂";
    /**
     * UI界面的宽度
     */
    private static final int WIDTH = 1000;
    /**
     * UI界面的高度
     */
    private static final int HEIGHT = 600;

    /**
     * JavaFX线程启动函数
     * @param stage 主窗口
     * @throws Exception 任何可能抛出的异常
     */
    @Override public void start(Stage stage) throws Exception {
        App.stage = stage;
        Platform.setImplicitExit(false);
        stage.getIcons().add(new Image(App.class.getResourceAsStream("/main.png"))); // 应用图标
        stage.initStyle(StageStyle.UNDECORATED); // 无边框窗体
        stage.setTitle(TITLE);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("app.fxml")); // 加载对应的UI设计文件
        Parent root = loader.load();
        Controller controller = loader.getController();
        controller.setStage(stage);
        Scene scene = new Scene(root, WIDTH, HEIGHT); // 初始化UI场景
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(e -> System.exit(0)); // 窗口退出时使JVM同时结束
    }
}
