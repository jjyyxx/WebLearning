package weblearning.v_2015;

import javafx.beans.property.StringProperty;
import okhttp3.HttpUrl;
import weblearning.Bulletin;
import weblearning.Client;
import weblearning.v_2015.message_model.NoticeMessage;

import java.util.concurrent.CompletableFuture;

/**
 * 单个公告对应的对象
 */
public class Bulletin2015 extends Bulletin {
    public static final String TRUE = "已读";
    private static final Client client = Client2015.getInstance();

    public static final String BULLETIN = "MultiLanguage/public/bbs/note_reply.jsp";

    public Bulletin2015(String name, String publisher, String time, String state, String content) {
        this.name.set(name);
        this.publisher.set(publisher);
        this.time.set(time);
        this.isRead.set(state);
        this.content.set(content);
    }

    /**
     * 请求公告的内容
     * @return 包含公告内容的CompletableFuture
     */
    @Override public CompletableFuture<StringProperty> resolveContent() {
        return CompletableFuture.completedFuture(content);
    }

    /**
     * 将公告标注为已读
     */
    @Override public CompletableFuture<Void> markAsRead() {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * 从html元素解析公告对象
     * @param entry 包含公告对象的html元素
     * @return 公告对象
     */
    static Bulletin from(NoticeMessage entry) {
        final String title = entry.courseNotice.title;
        final String publisher = entry.courseNotice.owner;
        final String time = entry.courseNotice.regDate;
        final String state = entry.status.trim().equals("0") ? TRUE : "未读";
        final String content = entry.courseNotice.detail;
        return new Bulletin2015(title, publisher, time, state, content);
    }

    @Override public HttpUrl getURL() {
        return client.makeUrl(BULLETIN, args);
    }
}
