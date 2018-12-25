package weblearning;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static common.Util.toLinkedMap;

/**
 * 老版网络学堂的单个课程
 */
public class CourseDataOld extends CourseData {
    private static final Pattern courseIdPattern = Pattern.compile("course_id=(\\d+)");
    private static final Pattern courseNamePattern = Pattern.compile("^(.*)\\((.+)\\)$");
    private static final String BULLETIN = "MultiLanguage/public/bbs/getnoteid_student.jsp";
    private static final String INFORMATION = "MultiLanguage/lesson/student/course_info.jsp";
    private static final String FILE = "MultiLanguage/lesson/student/download.jsp";
    private static final String RESOURCE = "MultiLanguage/lesson/student/ware_list.jsp";
    private static final String OPERATION = "MultiLanguage/lesson/student/hom_wk_brw.jsp";
    private static final String ALLSCORES = "MultiLanguage/lesson/student/hom_wk_recmark.jsp";
    //    private static final String FAQ = "MultiLanguage/public/bbs/getbbsid_student.jsp";
    //    private static final String DISCUSSION = "MultiLanguage/public/bbs/gettalkid_student.jsp";
    private static final Client client = Client.getInstance();

    CourseDataOld(String url, String name, String unsubmittedOperations, String unreadBulletins, String unreadFiles) {
        super(name, Version.V_OLD);
        this.unsubmittedOperations.set(Integer.valueOf(unsubmittedOperations));
        this.unreadBulletins.set(Integer.valueOf(unreadBulletins));
        this.unreadFiles.set(Integer.valueOf(unreadFiles));
        Matcher matcher = courseIdPattern.matcher(url);
        if (matcher.find()) {
            id = matcher.group(1);
        } else {
            throw new IllegalArgumentException();
        }
        matcher = courseNamePattern.matcher(name);
        if (matcher.find()) {
            this.name = matcher.group(1);
            semester = SemesterData.valueOf(matcher.group(2));
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * 获取课程公告
     */
    @Override public CompletableFuture<Bulletin[]> resolveBulletins() {
        return client.getAsync(client.makeUrl(BULLETIN, "course_id=" + id)).thenApply(document -> {
            Element tableBox = document.getElementById("table_box");
            Elements entries = tableBox.getElementsByTag("tr");
            if (entries.size() == 0) {
                return new Bulletin[]{};
            }
            Bulletin[] bulletins = new Bulletin[entries.size() - 1];
            for (int i = 1; i < entries.size(); i++) {
                Bulletin bulletin = Bulletin.from(entries.get(i));
                if (!bulletin.isRead.get().equals(Bulletin.TRUE)) {
                    bulletin.isRead.addListener((o, oV, nV) -> unreadBulletins.set(unreadBulletins.get() - 1));
                }
                bulletins[i - 1] = bulletin;
            }
            return bulletins;
        });
    }

    /**
     * 获取课程信息
     */
    @Override public CompletableFuture<Information> resolveInformation() {
        return client.getAsync(client.makeUrl(INFORMATION, "course_id=" + id)).thenApply(document -> {
            Element tableBox = document.getElementById("table_box").child(0);
            return Information.from(tableBox);
        });
    }

    /**
     * 获取课程文件
     */
    @Override public CompletableFuture<Map<String, FileEntry[]>> resolveFileEntries() {
        return client.getAsync(client.makeUrl(FILE, "course_id=" + id)).thenApply(document -> {
            Element nextGroup;
            int num = 0;
            Map<String, FileEntry[]> map = new LinkedHashMap<>();
            while ((nextGroup = document.getElementById("ImageTab" + ++num)) != null) {
                Elements entries = document.getElementById("Layer" + num).child(0).child(0).children();
                entries.remove(0);
                FileEntry[] fileEntries = new FileEntry[entries.size()];
                for (int i = 0; i < entries.size(); i++) {
                    FileEntry fileEntry = FileEntry.from(entries.get(i));
                    if (!fileEntry.isRead.get().equals(FileEntry.TRUE)) {
                        fileEntry.isRead.addListener((o, oV, nV) -> unreadFiles.set(unreadFiles.get() - 1));
                    }
                    fileEntries[i] = fileEntry;
                }
                map.put(nextGroup.text(), fileEntries);
            }
            return map;
        });
    }

    /**
     * 获取课程资源
     */
    @Override public CompletableFuture<Resource[]> resolveResources() {
        return client.getAsync(client.makeUrl(RESOURCE, "course_id=" + id)).thenApply(document -> {
            Elements entries = document.getElementById("table_box").nextElementSibling().child(0).children();
            entries.remove(entries.size() - 1);
            return entries.stream().map(Resource::from).toArray(Resource[]::new);
        });
    }

    /**
     * 获取课程作业
     */
    @Override public CompletableFuture<Operation[]> resolveOperations() {
        return client.getAsync(client.makeUrl(OPERATION, "course_id=" + id)).thenApply(document -> {
            Element tableBox = document.getElementById("table_box");
            if (tableBox == null) {
                return new Operation[]{};
            }
            Elements entries = tableBox.nextElementSibling().child(0).children();
            entries.remove(entries.size() - 1);
            Operation[] operations = new Operation[entries.size()];
            for (int i = 0; i < entries.size(); i++) {
                Operation operation = Operation.from(entries.get(i));
                if (!operation.isHandedIn.get().equals(Operation.TRUE)) {
                    operation.isHandedIn.addListener((o, oV, nV) -> unsubmittedOperations.set(unsubmittedOperations.get() - 1));
                }
                operations[i] = operation;
            }
            return operations;
        });
    }

    /**
     * 获取课程作业分数
     */
    @Override public CompletableFuture<Map<String, String>> resolveAllOperationScores() {
        return client.getAsync(client.makeUrl(ALLSCORES, "course_id=" + id)).thenApply(document -> {
            Elements entries = document.getElementById("Layer1").child(0).child(0).children();
            entries.remove(entries.size() - 1);
            return entries.stream().collect(toLinkedMap(e -> e.child(0).text(), e -> e.child(1).text()));
        });
    }

    @Override public String getUrl() {
        return "/MultiLanguage/lesson/student/course_locate.jsp?course_id=" + id;
    }
}
