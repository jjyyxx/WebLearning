package weblearning;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.jsoup.nodes.Element;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class CourseData {
    protected String id;
    protected String name;
    protected Version version;
    protected SemesterData semester;
    public final IntegerProperty unsubmittedOperations = new SimpleIntegerProperty();
    public final IntegerProperty unreadBulletins = new SimpleIntegerProperty();
    public final IntegerProperty unreadFiles = new SimpleIntegerProperty();

    CourseData(String name, Version version) {
        this.name = name;
        this.version = version;
    }

    public abstract CompletableFuture<Bulletin[]> resolveBulletins();

    public abstract CompletableFuture<Information> resolveInformation();

    public abstract CompletableFuture<Map<String, FileEntry[]>> resolveFileEntries();

    public abstract CompletableFuture<Resource[]> resolveResources();

    public abstract CompletableFuture<Operation[]> resolveOperations();

    public abstract CompletableFuture<Map<String, String>> resolveAllOperationScores();

    public abstract String getUrl();

    public String getName() {
        return name;
    }

    public Version getVersion() {
        return version;
    }

    public SemesterData getSemester() {
        return semester;
    }

    static CourseData from(Element entry) {
        Element link = entry.child(0).child(1);
        String href = link.attr("href");
        String name = link.text();
        if (href.startsWith("http://learn2018.")) {
            return new CourseData2018(href, name, "0", "0", "0");
        } else {
            String operations = entry.child(1).child(0).text();
            String notices = entry.child(2).child(0).text();
            String files = entry.child(3).child(0).text();
            if (href.startsWith("http://learn.cic.")) {
                return new CourseData2015(href, name, operations, notices, files);
            } else {
                return new CourseDataOld(href, name, operations, notices, files);
            }
        }
    }
}
