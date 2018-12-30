package background;

import app.Util;
import common.Settings;
import okhttp3.HttpUrl;
import weblearning.Client;
import weblearning.CourseData;
import weblearning.FileEntry;
import weblearning.Operation;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 在后台执行下载任务，管理各类下载请求
 */
public class DownloadManager {
    private static final Pattern filenamePattern = Pattern.compile("filename=\".*(\\.\\w+)\"$");

    /**
     * 用于将url的文件下载至dir目录下的filename文件
     * @param client 对应的web请求客户端
     * @param dir 目标目录
     * @param url 下载链接
     * @param filename 文件名（不含后缀）
     * @return 包含DownloadInfo的CompletableFuture
     */
    private static CompletableFuture<DownloadInfo> download(Client client, Path dir, HttpUrl url, String filename) {
        return client.getRawAsync(url).thenApply(response -> {
            String contentDisposition = response.header("Content-Disposition");
            Matcher matcher = filenamePattern.matcher(contentDisposition);
            matcher.find();
            String suffix = matcher.group(1);
            return new DownloadInfo(dir, filename, suffix, response.body().byteStream());
        });
    }

    /**
     * 下载并打开文件
     * @param courseData 文件所属课程
     * @param entries 所需要下载的文件
     * @param open 是否需要打开
     */
    public static void enqueue(CourseData courseData, FileEntry[] entries, boolean open) {
        Path saveDir = getPath(courseData);
        if (saveDir == null) {
            return;
        }
        for (FileEntry entry : entries) {
            download(courseData.getClient(), saveDir, entry.getURL(), entry.title.get()).thenAccept(downloadInfo -> {
                try {
                    Files.copy(downloadInfo.inputStream, downloadInfo.path);
                    Util.showSnackBar(downloadInfo.path + "下载完成", 1000, "success");
                    if (open) {
                        Desktop.getDesktop().open(downloadInfo.path.toFile());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * 获取用户选择的存储地址并记录为下一次下载的默认地址
     * @param courseData 文件所属课程
     * @return 下载的目标目录
     */
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

    /**
     * 下载并打开作业附件
     * @param courseData 作业所属课程
     * @param operation 所需要下载附件所属的作业
     */
    public static void enqueue(CourseData courseData, Operation operation) {
        Path saveDir = getPath(courseData);
        if (saveDir == null) {
            return;
        }
        download(courseData.getClient(), saveDir, operation.getAttachmentUrl(), operation.getAttachmentName()).thenAccept(downloadInfo -> {
            try {
                Files.copy(downloadInfo.inputStream, downloadInfo.path);
                Util.showSnackBar(downloadInfo.path + "下载完成", 1000, "success");
                Desktop.getDesktop().open(downloadInfo.path.toFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
