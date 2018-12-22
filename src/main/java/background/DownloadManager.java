package background;

import common.Settings;
import okhttp3.HttpUrl;
import weblearning.Client;
import weblearning.CourseData;
import weblearning.FileEntry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadManager {

    private static final Client client = Client.getInstance();
    private static final Pattern filenamePattern = Pattern.compile("filename=\"([^\"]*)\"$");


    public static CompletableFuture<Boolean> download(Path dir, HttpUrl url, String filename) {
        return client.getRawAsync(url).thenApply(response -> {
            String contentDisposition = response.header("Content-Disposition");
            Matcher matcher = filenamePattern.matcher(contentDisposition);
            matcher.find();
            String[] preparaionSuffix = matcher.group(1).split("\\.");
            String suffix = preparaionSuffix[preparaionSuffix.length-1];

            try {
                Files.copy(response.body().byteStream(), dir.resolve(filename+"."+suffix));
                return true;
            } catch (IOException e) {
                return false;
            }
        });
    }

    public static void enqueue(CourseData courseData, FileEntry entry) {
        Path saveDir = getPath(courseData);
        if (saveDir == null)
        {
            return;
        }
        download(saveDir,entry.getURL(),entry.title.get());

    }

    public static void enqueue(CourseData courseData, FileEntry[] entries) {
        Path saveDir = getPath(courseData);
        if (saveDir == null)
        {
            return;
        }
        for(FileEntry entry:entries)
        {
            download(saveDir,entry.getURL(),entry.title.get());

        }
    }

    private static Path getPath(CourseData courseData)
    {
        Path saveDir;
        if(Settings.INSTANCE.separateByCourse.get())
        {
            saveDir = app.Util.requestDir(courseData.getLastDir());
            if (saveDir == null)
            {
                return null;
            }
            courseData.setLastDir(saveDir);
        }
        else
        {
            saveDir = app.Util.requestDir(CourseData.defaultDir);
            if (saveDir == null)
            {
                return null;
            }
            CourseData.defaultDir=saveDir;
        }
        return saveDir;
    }
}
