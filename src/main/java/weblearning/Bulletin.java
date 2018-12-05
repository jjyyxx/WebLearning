package weblearning;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.jsoup.nodes.Element;

import java.util.concurrent.CompletableFuture;

import static common.Util.getArg;

public class Bulletin extends RecursiveTreeObject<Bulletin> {
    public static final String TRUE = "已读";
    private static final Client client = Client.getInstance();

    private static final String BULLETIN = "MultiLanguage/public/bbs/note_reply.jsp";

    private String args;
    public final StringProperty name = new SimpleStringProperty();
    public final StringProperty publisher = new SimpleStringProperty();
    public final StringProperty time = new SimpleStringProperty();
    public final StringProperty isRead = new SimpleStringProperty();
    public boolean contentResolved = false;
    public final StringProperty content = new SimpleStringProperty();

    public Bulletin() {}

    private Bulletin(String url, String name, String publisher, String time, String state) {
        this.args = getArg(url);
        this.name.set(name);
        this.publisher.set(publisher);
        this.time.set(time);
        this.isRead.set(state);
        this.content.set("");
    }

    public CompletableFuture<StringProperty> resolveContent() {
        if (this.contentResolved) {
            return CompletableFuture.completedFuture(content);
        }
        return client.getAsync(client.makeUrl(BULLETIN, args)).thenApply(document -> {
            Element tableBox = document.getElementById("table_box");
            this.content.set(tableBox.child(0).child(1).text().substring(3));
            contentResolved = true;
            isRead.set(TRUE);
            return this.content;
        });
    }

    public CompletableFuture<Void> markAsRead() {
        if (!isRead.get().equals(TRUE)) {
            return CompletableFuture.completedFuture(null);
        }
        return client.getRawAsync(client.makeUrl(BULLETIN, args)).thenAccept(response -> {
            isRead.set(TRUE);
            response.close();
        });
    }

    static Bulletin from(Element entry) {
        Element link = entry.child(1).child(0);
        String href = link.attr("href");
        String title = link.text();
        String publisher = entry.child(2).text();
        String time = entry.child(3).text();
        String state = entry.child(4).text();
        return new Bulletin(href, title, publisher, time, state);
    }
}
