package weblearning;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;

public class Courses extends LinkedHashMap<String, CourseData> {
    void update(Element document) {
        Elements entries = document.getElementById("info_1").getElementsByTag("tr");
        entries.subList(2, entries.size()).forEach(entry -> {
            String name = entry.child(0).child(1).text();
            String operations = entry.child(1).child(0).text();
            String notices = entry.child(2).child(0).text();
            String files = entry.child(3).child(0).text();
            this.get(name).update(operations, notices, files);
        });
    }

    static Courses from(Element document) {
        Elements entries = document.getElementById("info_1").getElementsByTag("tr");
        Courses courses = new Courses();
        entries.subList(2, entries.size()).forEach(entry -> {
            Element link = entry.child(0).child(1);
            String href = link.attr("href");
            String name = link.text();
            String operations = entry.child(1).child(0).text();
            String notices = entry.child(2).child(0).text();
            String files = entry.child(3).child(0).text();
            courses.put(name, new CourseData(href, name, operations, notices, files));
        });
        return courses;
    }
}
