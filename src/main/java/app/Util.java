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

public class Util {
    static Path requestDir(Path initialDir) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("选择目标文件夹");
        if (initialDir != null) {
            chooser.setInitialDirectory(initialDir.toFile());
        }
        File file = chooser.showDialog(App.stage);
        return file == null ? null : file.toPath();
    }

    static Path requestOpenFile(Path initialDir) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("选择打开的文件");
        if (initialDir != null) {
            chooser.setInitialDirectory(initialDir.toFile());
        }
        File file = chooser.showOpenDialog(App.stage);
        return file == null ? null : file.toPath();
    }

    static Path requestSaveFile(Path initialDir) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("选择保存的文件");
        if (initialDir != null) {
            chooser.setInitialDirectory(initialDir.toFile());
        }
        File file = chooser.showSaveDialog(App.stage);
        return file == null ? null : file.toPath();
    }

    static void requestTime(StackPane main, Consumer<Date> consumer) {
        Locale.setDefault(Locale.ENGLISH);
        JFXDialog jfxDialog = new JFXDialog();
        JFXDialogLayout layout = new JFXDialogLayout();
        layout.setHeading(new Label("请选择日期和时间"));
        JFXDatePicker jfxDatePicker = new JFXDatePicker();
        JFXTimePicker jfxTimePicker = new JFXTimePicker();
        JFXButton commit = new JFXButton();
        commit.setText("确定");
        commit.setOnAction(actionEvent -> {
            LocalDate date = jfxDatePicker.getValue();
            LocalTime time = jfxTimePicker.getValue();
            if (date != null && time != null && LocalDateTime.now(ZoneId.systemDefault()).isBefore(date.atTime(time))) {
                jfxDialog.close();
                Locale.setDefault(Locale.CHINA);
                consumer.accept(Date.from(date.atTime(time).atZone(ZoneId.systemDefault()).toInstant()));
            }
        });
        JFXButton cancel = new JFXButton();
        cancel.setText("取消");
        cancel.setOnAction(actionEvent -> {
            jfxDialog.close();
            Locale.setDefault(Locale.CHINA);
            consumer.accept(null);
        });
        layout.setBody(new VBox(jfxDatePicker, jfxTimePicker));
        layout.setActions(commit, cancel);
        jfxDialog.setContent(layout);
        jfxDialog.show(main);
    }
}
