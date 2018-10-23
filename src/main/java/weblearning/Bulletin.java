package WebLearning;

import okhttp3.Response;
import org.jsoup.nodes.Element;

import java.util.concurrent.CompletableFuture;

import static Common.Util.getArg;

public class Bulletin {
    private static final Client client = Client.getInstance();

    private static final String BULLETIN = "MultiLanguage/public/bbs/note_reply.jsp";

    private String args;
    private String name;
    private String publisher;
    private String time;
    private boolean isRead;

    private String content;

    private Bulletin(String url, String name, String publisher, String time, String state) {
        this.args = getArg(url);
        this.name = name;
        this.publisher = publisher;
        this.time = time;
        this.isRead = state.equals("已读");
    }

    public CompletableFuture<String> resolveContent() {
        return client.getAsync(client.makeUrl(BULLETIN, args)).thenApply(document -> {
            Element tableBox = document.getElementById("table_box");
            return this.content = tableBox.child(0).child(0).child(1).text();
        });
    }

    public CompletableFuture<Void> markAsRead() {
        return client.getRawAsync(client.makeUrl(BULLETIN, args)).thenAccept(Response::close);
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
