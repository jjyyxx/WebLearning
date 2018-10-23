package weblearning;

import org.jsoup.nodes.Element;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static common.Util.getArg;

public class FileEntry {
    private static final Client client = Client.getInstance();
    private static final String url = "uploadFile/downloadFile_student.jsp";
    private static final Pattern filenamePattern = Pattern.compile("filename=\"([^\"]*)\"$");

    private String args;
    private String title;
    private String description;
    private String size;
    private String uploadTime;
    private boolean isRead;

    private FileEntry(String url, String title, String description, String size, String uploadTime, String state) {
        this.args = getArg(url);
        this.title = title;
        this.description = description;
        this.size = size;
        this.uploadTime = uploadTime;
        this.isRead = state.equals("新文件");
    }

    public CompletableFuture<Boolean> download(Path dir) {
        return client.getRawAsync(client.makeUrl(url, args)).thenApply(response -> {
            String contentDisposition = response.header("Content-Disposition");
            Matcher matcher = filenamePattern.matcher(contentDisposition);
            matcher.find();
            String filename = matcher.group(1);
            try {
                Files.copy(response.body().byteStream(), dir.resolve(filename));
                return true;
            } catch (IOException e) {
                return false;
            }
        });
    }

    static FileEntry from(Element entry) {
        Element link = entry.child(1).child(0);
        String href = link.attr("href");
        String title = link.text();
        String description = entry.child(2).text();
        String size = entry.child(3).text();
        String time = entry.child(4).text();
        String state = entry.child(5).text();
        return new FileEntry(href, title, description, size, time, state);
    }
}
