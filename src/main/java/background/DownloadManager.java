package background;

import app.Controller;
import com.jfoenix.controls.JFXSnackbar;
import common.Settings;
import okhttp3.HttpUrl;
import weblearning.Client;
import weblearning.CourseData;
import weblearning.FileEntry;
import weblearning.Operation;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadManager {
    static class DownloadInfo {
        private final InputStream inputStream;
        private final Path dir;
        private final String name;
        private final String ext;
        private final Path path;

        public DownloadInfo(Path dir, String name, String ext, InputStream inputStream) {
            this.dir = dir;
            this.name = name;
            this.ext = ext;
            this.inputStream = inputStream;
            this.path = getUnexistPath();
        }

        public Path getUnexistPath() {
            Path path = dir.resolve(name + ext);
            int x = 0;
            while (Files.exists(path)) {
                path = dir.resolve(name + "(" + ++x + ")" + ext);
            }
            return path;
        }
    }

    private static final Client client = Client.getInstance();
    private static final Pattern filenamePattern = Pattern.compile("filename=\".*(\\.\\w+)\"$");

    private static CompletableFuture<DownloadInfo> download(Path dir, HttpUrl url, String filename) {
        return client.getRawAsync(url).thenApply(response -> {
            String contentDisposition = response.header("Content-Disposition");
            Matcher matcher = filenamePattern.matcher(contentDisposition);
            matcher.find();
            String suffix = matcher.group(1);
            return new DownloadInfo(dir, filename, suffix, response.body().byteStream());
        });
    }

    public static void enqueue(CourseData courseData, FileEntry[] entries, boolean open) {
        Path saveDir = getPath(courseData);
        if (saveDir == null) {
            return;
        }
        for (FileEntry entry : entries) {
            download(saveDir, entry.getURL(), entry.title.get()).thenAccept(downloadInfo -> {
                try {
                    Files.copy(downloadInfo.inputStream, downloadInfo.path);
                    Controller.snackBar.enqueue(new JFXSnackbar.SnackbarEvent(downloadInfo.path + "下载完成", "success", null, 1000, false, null));
                    if (open) {
                        Desktop.getDesktop().open(downloadInfo.path.toFile());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private static Path getPath(CourseData courseData) {
        Path saveDir;
        if (Settings.INSTANCE.separateByCourse.get()) {
            saveDir = app.Util.requestDir(Settings.INSTANCE.pathRegistry.get(courseData.getName()));
            Settings.INSTANCE.pathRegistry.put(courseData.getName(), saveDir);
        } else {
            saveDir = app.Util.requestDir(Settings.INSTANCE.pathRegistry.get("DEFAULT"));
            Settings.INSTANCE.pathRegistry.put("DEFAULT", saveDir);
        }
        return saveDir;
    }

    public static void enqueue(CourseData courseData, Operation operation) {
        Path saveDir = getPath(courseData);
        if (saveDir == null) {
            return;
        }
        download(saveDir, operation.getAttachmentUrl(), operation.getAttachmentName()).thenAccept(downloadInfo -> {
            try {
                Files.copy(downloadInfo.inputStream, downloadInfo.path);
                Controller.snackBar.enqueue(new JFXSnackbar.SnackbarEvent(downloadInfo.path + "下载完成", "success", null, 1000, false, null));
                Desktop.getDesktop().open(downloadInfo.path.toFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
