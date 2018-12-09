package app;

import javafx.stage.DirectoryChooser;

import java.io.File;
import java.nio.file.Path;

public class Util {
    static Path requestDir() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("选择目标文件夹");
        File file = chooser.showDialog(App.stage);
        return file == null ? null : file.toPath();
    }

    static Path requestDir(Path initialDir) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("选择目标文件夹");
        chooser.setInitialDirectory(initialDir.toFile());
        File file = chooser.showDialog(App.stage);
        return file == null ? null : file.toPath();
    }
}
