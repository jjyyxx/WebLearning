package weblearning;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.jsoup.nodes.Element;
import weblearning.v_2015.CourseData2015;
import weblearning.v_2018.CourseData2018;
import weblearning.v_old.CourseDataOld;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 抽象类，将为不同版本网络学堂提供一致的api
 */
public abstract class CourseData {
    protected String id;
    protected String name;
    protected Version version;
    public final IntegerProperty unsubmittedOperations = new SimpleIntegerProperty();
    public final IntegerProperty unreadBulletins = new SimpleIntegerProperty();
    public final IntegerProperty unreadFiles = new SimpleIntegerProperty();

    protected CourseData(String name, Version version) {
        this.name = name;
        this.version = version;
    }

    /**
     * 获取课程公告
     */
    public abstract CompletableFuture<Bulletin[]> resolveBulletins();

    /**
     * 获取课程文件
     */
    public abstract CompletableFuture<Map<String, FileEntry[]>> resolveFileEntries();

    /**
     * 获取课程作业
     */
    public abstract CompletableFuture<Operation[]> resolveOperations();

    /**
     * 获取课程作业分数
     */
    public abstract CompletableFuture<Map<String, String>> resolveAllOperationScores();

    public abstract String getUrl();

    public String getName() {
        return name;
    }

    public Version getVersion() {
        return version;
    }

    public abstract Client getClient();

    /**
     * 将课程分流到具体元素上
     */
    static CourseData from(Element entry) {
        Element link = entry.child(0).child(1);
        String href = link.attr("href");
        String name = link.text();
        try {
            String operations = entry.child(1).child(0).text();
            String notices = entry.child(2).child(0).text();
            String files = entry.child(3).child(0).text();
            if (href.startsWith("http://learn.cic.")) {
                return new CourseData2015(href, name, operations, notices, files);
            } else {
                return new CourseDataOld(href, name, operations, notices, files);
            }
        } catch (IndexOutOfBoundsException e) {
            return new CourseData2018(href, name, "0", "0", "0");
        }
    }
}
