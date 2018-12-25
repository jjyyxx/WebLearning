package app;

import com.jfoenix.controls.*;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * 几个可重用的、具有简单返回值的UI部件，包括打开文件/文件夹、请求日期等
 */
public class Util {
    /**
     * 提示用户选择文件夹的函数，以下两函数同理，不再赘述
     * @see Util#requestOpenFile(Path)
     * @see Util#requestSaveFile(Path)
     * @param initialDir 初始目录
     * @return 用户选择的路径，保证是文件夹，如果用户没有选择文件夹而退出了窗口将返回null
     */
    public static Path requestDir(Path initialDir) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("选择目标文件夹");
        if (initialDir != null) {
            chooser.setInitialDirectory(initialDir.toFile());
        }
        File file = chooser.showDialog(App.stage);
        return file == null ? null : file.toPath();
    }

    public static Path requestOpenFile(Path initialDir) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("选择打开的文件");
        if (initialDir != null) {
            chooser.setInitialDirectory(initialDir.toFile());
        }
        File file = chooser.showOpenDialog(App.stage);
        return file == null ? null : file.toPath();
    }

    public static Path requestSaveFile(Path initialDir) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("选择保存的文件");
        if (initialDir != null) {
            chooser.setInitialDirectory(initialDir.toFile());
        }
        File file = chooser.showSaveDialog(App.stage);
        return file == null ? null : file.toPath();
    }

    // 避免重复构造，提升性能，见static initializer
    private static final JFXDialog jfxDialog;
    private static Consumer<Date> consumer;

    static {
        jfxDialog = new JFXDialog();
        JFXDialogLayout layout = new JFXDialogLayout();
        layout.setHeading(new Label("请选择日期和时间"));
        JFXDatePicker jfxDatePicker = new JFXDatePicker();
        JFXTimePicker jfxTimePicker = new JFXTimePicker();
        JFXButton commit = new JFXButton();
        commit.setText("确定");
        commit.setOnAction(actionEvent -> {
            LocalDate date = jfxDatePicker.getValue();
            LocalTime time = jfxTimePicker.getValue();
            // 判断值是否填写并且有效
            if (date != null && time != null && LocalDateTime.now(ZoneId.systemDefault()).isBefore(date.atTime(time))) {
                jfxDialog.close();
                // 一个UI库的bug的workaround，见https://github.com/jfoenixadmin/JFoenix/pull/894
                Locale.setDefault(Locale.CHINA);
                consumer.accept(Date.from(date.atTime(time).atZone(ZoneId.systemDefault()).toInstant()));
                consumer = null;
            } else {
                // 向用户提供反馈
                Controller.snackBar.enqueue(new JFXSnackbar.SnackbarEvent("时间填写不完整或早于当前时间", "error"));
            }
        });
        JFXButton cancel = new JFXButton();
        cancel.setText("取消");
        cancel.setOnAction(actionEvent -> {
            jfxDialog.close();
            // 一个UI库的bug的workaround，见https://github.com/jfoenixadmin/JFoenix/pull/894
            Locale.setDefault(Locale.CHINA);
            consumer = null;
        });
        layout.setBody(new VBox(jfxDatePicker, jfxTimePicker));
        layout.setActions(commit, cancel);
        jfxDialog.setContent(layout);
    }

    static void requestTime(StackPane main, Consumer<Date> consumer) {
        // 异步操作的回调函数，接受一个Date类型的返回值，避免阻塞主线程
        Util.consumer = consumer;
        // 一个UI库的bug的workaround，见https://github.com/jfoenixadmin/JFoenix/pull/894
        Locale.setDefault(Locale.ENGLISH);
        // 显示对话框
        jfxDialog.show(main);
    }
}
