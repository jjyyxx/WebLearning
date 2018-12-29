package weblearning.v_old;

import okhttp3.HttpUrl;
import org.jsoup.nodes.Element;
import weblearning.Client;
import weblearning.FileEntry;

import static common.Util.getArg;

/**
 * 单个文件对应的对象
 */
public class FileEntryT extends FileEntry {
    public static final String TRUE = "已读";
    private static final Client client = Client.getInstance();
    private static final String DOWNLOAD = "uploadFile/downloadFile_student.jsp";

    protected FileEntryT(String url, String title, String description, String size, String uploadTime, String state) {
        this.args = getArg(url);
        this.title.set(title);
        this.description.set(description);
        this.size.set(size);
        this.uploadTime.set(uploadTime);
        this.isRead.set(state);
    }

    /**
     * 从html元素解析文件对象
     * @param entry 包含文件对象的html元素
     * @return 文件对象
     */
    public static FileEntry from(Element entry) {
        Element link = entry.child(1).child(0);
        String href = link.attr("href");
        String title = link.text();
        String description = entry.child(2).text();
        String size = entry.child(3).text();
        String time = entry.child(4).text();
        String state = entry.child(5).text().equals("新文件") ? "未读" : "已读";
        return new FileEntryT(href, title, description, size, time, state);
    }

    @Override public HttpUrl getURL() {
        return client.makeUrl(DOWNLOAD, args);
    }
}
