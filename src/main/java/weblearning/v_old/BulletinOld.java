package weblearning.v_old;

import javafx.beans.property.StringProperty;
import okhttp3.HttpUrl;
import org.jsoup.nodes.Element;
import weblearning.Bulletin;
import weblearning.Client;

import java.util.concurrent.CompletableFuture;

import static common.Util.getArg;

/**
 * 单个公告对应的对象
 */
public class BulletinOld extends Bulletin {
    public static final String TRUE = "已读";
    public static final Client client = Client.getInstance();

    public static final String BULLETIN = "MultiLanguage/public/bbs/note_reply.jsp";

    public BulletinOld(String url, String name, String publisher, String time, String state) {
        this.args = getArg(url);
        this.name.set(name);
        this.publisher.set(publisher);
        this.time.set(time);
        this.isRead.set(state);
        this.content.set("");
    }

    /**
     * 请求公告的内容
     * @return 包含公告内容的CompletableFuture
     */
    @Override public CompletableFuture<StringProperty> resolveContent() {
        if (this.contentResolved) {
            return CompletableFuture.completedFuture(content);
        }
        return client.getAsync(getURL()).thenApply(document -> {
            Element tableBox = document.getElementById("table_box");
            this.content.set(tableBox.child(0).child(1).text().substring(3));
            contentResolved = true;
            isRead.set(TRUE);
            return this.content;
        });
    }

    /**
     * 将公告标注为已读
     */
    @Override public CompletableFuture<Void> markAsRead() {
        if (!isRead.get().equals(TRUE)) {
            return CompletableFuture.completedFuture(null);
        }
        return client.getRawAsync(client.makeUrl(BULLETIN, args)).thenAccept(response -> {
            isRead.set(TRUE);
            response.close();
        });
    }

    /**
     * 从html元素解析公告对象
     * @param entry 包含公告对象的html元素
     * @return 公告对象
     */
    static Bulletin from(Element entry) {
        Element link = entry.child(1).child(0);
        String href = link.attr("href");
        String title = link.text();
        String publisher = entry.child(2).text();
        String time = entry.child(3).text();
        String state = entry.child(4).text();
        return new BulletinOld(href, title, publisher, time, state);
    }

    @Override public HttpUrl getURL() {
        return client.makeUrl(BULLETIN, args);
    }
}
