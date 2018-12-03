package weblearning;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static common.Util.getArg;

public class FileEntry extends RecursiveTreeObject<FileEntry> {
    private static final Client client = Client.getInstance();
    private static final String url = "uploadFile/downloadFile_student.jsp";
    private static final Pattern filenamePattern = Pattern.compile("filename=\"([^\"]*)\"$");

    private String args;
    public final StringProperty title = new SimpleStringProperty();
    public final StringProperty description = new SimpleStringProperty();
    public final StringProperty size = new SimpleStringProperty();
    public final StringProperty uploadTime = new SimpleStringProperty();
    public final BooleanProperty isRead = new SimpleBooleanProperty();

    private FileEntry(String url, String title, String description, String size, String uploadTime, String state) {
        this.args = getArg(url);
        this.title.set(title);
        this.description.set(description);
        this.size.set(size);
        this.uploadTime.set(uploadTime);
        this.isRead.set(!state.equals("新文件"));
    }

    public CompletableFuture<Boolean> download(Path dir) {
        return client.getRawAsync(client.makeUrl(url, args)).thenApply(response -> {
            this.isRead.set(true);
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
