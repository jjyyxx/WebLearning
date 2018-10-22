package WebLearning;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Common.Util.toLinkedMap;

public class CourseData {
    private static final Pattern courseIdPattern = Pattern.compile("course_id=(\\d+)");
    private static final Pattern courseNamePattern = Pattern.compile("^(.*)\\((.+)\\)$");

    private static final String BULLETIN = "MultiLanguage/public/bbs/getnoteid_student.jsp";
    private static final String INFOMATION = "MultiLanguage/lesson/student/course_info.jsp";
    private static final String FILE = "MultiLanguage/lesson/student/download.jsp";
    private static final String RESOURCE = "MultiLanguage/lesson/student/ware_list.jsp";
    private static final String OPERATION = "MultiLanguage/lesson/student/hom_wk_brw.jsp";
    private static final String ALLSCORES = "MultiLanguage/lesson/student/hom_wk_recmark.jsp";
    private static final String FAQ = "MultiLanguage/public/bbs/getbbsid_student.jsp";
    private static final String DISCUSSION = "MultiLanguage/public/bbs/gettalkid_student.jsp";

    private static final Client client = Client.getInstance();

    private String id;
    private String name;
    private boolean isNewVer;
    private SemesterData semester;
    private int unsubmittedOperations;
    private int unreadBulletins;
    private int unreadFiles;

    CourseData(String url, String name, String unsubmittedOperations, String unreadBulletins, String unreadFiles) {
        this.unsubmittedOperations = Integer.valueOf(unsubmittedOperations);
        this.unreadBulletins = Integer.valueOf(unreadBulletins);
        this.unreadFiles = Integer.valueOf(unreadFiles);
        isNewVer = url.startsWith("http://learn.cic.tsinghua.edu.cn");
        if (isNewVer) {
            id = url.substring(54);
        }
        else {
            Matcher matcher = courseIdPattern.matcher(url);
            if (matcher.find()) {
                id = matcher.group(1);
            } else {
                throw new IllegalArgumentException();
            }
        }
        Matcher matcher = courseNamePattern.matcher(name);
        if (matcher.find()) {
            this.name = matcher.group(1);
            semester = SemesterData.valueOf(matcher.group(2));
        } else {
            throw new IllegalArgumentException();
        }
    }

    public CompletableFuture<Bulletin[]> resolveBulletins() {
        return client.getAsync(client.makeUrl(BULLETIN, "course_id=" + id)).thenApply(document -> {
            Element tableBox = document.getElementById("table_box");
            Elements entries = tableBox.getElementsByTag("tr");
            return entries.subList(1, entries.size())
                    .stream()
                    .map(Bulletin::from)
                    .toArray(Bulletin[]::new);
        });
    }

    public CompletableFuture<Information> resolveInfomation() {
        return client.getAsync(client.makeUrl(INFOMATION, "course_id=" + id)).thenApply(document -> {
            Element tableBox = document.getElementById("table_box").child(0);
            return Information.from(tableBox);
        });
    }

    public CompletableFuture<Map<String, FileEntry[]>> resolveFileEntries() {
        return client.getAsync(client.makeUrl(FILE, "course_id=" + id)).thenApply(document -> {
            Element nextGroup;
            int num = 0;
            Map<String, FileEntry[]> map = new LinkedHashMap<>();
            while ((nextGroup = document.getElementById("ImageTab" + ++num)) != null) {
                Elements entries = document.getElementById("Layer" + num).child(0).child(0).children();
                entries.remove(0);
                map.put(nextGroup.text(), entries.stream().map(FileEntry::from).toArray(FileEntry[]::new));
            }
            return map;
        });
    }

    public CompletableFuture<Resource[]> resolveResources() {
        return client.getAsync(client.makeUrl(RESOURCE, "course_id=" + id)).thenApply(document -> {
            Elements entries = document.getElementById("table_box").nextElementSibling().child(0).children();
            entries.remove(entries.size() - 1);
            return entries.stream().map(Resource::from).toArray(Resource[]::new);
        });
    }

    public CompletableFuture<Operation[]> resolveOperations() {
        return client.getAsync(client.makeUrl(OPERATION, "course_id=" + id)).thenApply(document -> {
            Elements entries = document.getElementById("table_box").nextElementSibling().child(0).children();
            entries.remove(entries.size() - 1);
            return entries.stream().map(Operation::from).toArray(Operation[]::new);
        });
    }

    public CompletableFuture<Map<String, String>> resolveAllOperationScores() {
        return client.getAsync(client.makeUrl(ALLSCORES, "course_id=" + id)).thenApply(document -> {
            Elements entries = document.getElementById("Layer1").child(0).child(0).children();
            entries.remove(entries.size() - 1);
            return entries.stream().collect(toLinkedMap(e -> e.child(0).text(), e -> e.child(1).text()));
        });
    }

    public int getUnsubmittedOperations() {
        return unsubmittedOperations;
    }

    public int getUnreadFiles() {
        return unreadFiles;
    }

    public int getUnreadBulletins() {
        return unreadBulletins;
    }

    public boolean isNewVer() {
        return isNewVer;
    }

    public String getUrl() {
        return isNewVer ? "/MultiLanguage/lesson/student/course_locate.jsp?course_id=" + id : "http://learn.cic.tsinghua.edu.cn/f/student/coursehome/" + id;
    }

    public String getName() {
        return name;
    }

    public SemesterData getSemester() {
        return semester;
    }
}

enum SemesterType {
    None,
    Autumn,
    Spring,
    Summer;

    @Override
    public String toString() {
        switch (this) {
            case Autumn:
                return "秋季";
            case Spring:
                return "春季";
            case Summer:
                return "夏季";
        }
        return "";
    }

    public static SemesterType from(String semester) {
        switch (semester) {
            case "秋季":
                return Autumn;
            case "春季":
                return Spring;
            case "夏季":
                return Autumn;
        }
        return None;
    }
}

class SemesterData {
    private int start;
    private SemesterType type;
    private SemesterData(int start, SemesterType type) {
        this.start = start;
        this.type = type;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return start + 1;
    }

    public SemesterType getType() {
        return type;
    }

    @Override
    public String toString() {
        return start + "-" + (start + 1) + type + "学期";
    }

    private static final Pattern semesterPattern = Pattern.compile("(\\d+)-\\d+(.+)学期");

    static SemesterData valueOf(String semester) {
        Matcher matcher = semesterPattern.matcher(semester);
        if (matcher.find()) {
            return new SemesterData(Integer.valueOf(matcher.group(1)), SemesterType.from(matcher.group(2)));
        } else {
            throw new IllegalArgumentException();
        }
    }
}