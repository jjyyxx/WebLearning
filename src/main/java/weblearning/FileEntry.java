package weblearning;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import common.Navigable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import okhttp3.HttpUrl;
import org.jsoup.nodes.Element;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import static common.Util.getArg;

public class FileEntry extends RecursiveTreeObject<FileEntry> implements Navigable {
    public static final String TRUE = "已读";
    private static final Client client = Client.getInstance();
    private static final String DOWNLOAD = "uploadFile/downloadFile_student.jsp";
    private static final Pattern filenamePattern = Pattern.compile("filename=\"([^\"]*)\"$");

    private String args;
    public final StringProperty title = new SimpleStringProperty();
    public final StringProperty description = new SimpleStringProperty();
    public final StringProperty size = new SimpleStringProperty();
    public final StringProperty uploadTime = new SimpleStringProperty();
    public final StringProperty isRead = new SimpleStringProperty();

    private FileEntry(String url, String title, String description, String size, String uploadTime, String state) {
        this.args = getArg(url);
        this.title.set(title);
        this.description.set(description);
        this.size.set(size);
        this.uploadTime.set(uploadTime);
        this.isRead.set(state);
    }

    public CompletableFuture<Boolean> download(Path dir) {
        return Endpoints.download(dir, DOWNLOAD, args).thenApply(aBoolean -> {
            this.isRead.set(TRUE);
            return aBoolean;
        });
    }

    static FileEntry from(Element entry) {
        Element link = entry.child(1).child(0);
        String href = link.attr("href");
        String title = link.text();
        String description = entry.child(2).text();
        String size = entry.child(3).text();
        String time = entry.child(4).text();
        String state = entry.child(5).text().equals("新文件") ? "未读" : "已读";
        return new FileEntry(href, title, description, size, time, state);
    }

    @Override public HttpUrl getURL() {
        return client.makeUrl(DOWNLOAD, args);
    }
}
