package weblearning;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;

public class Courses extends LinkedHashMap<String, CourseData> {
    static Courses from(Element document) {
        Elements entries = document.getElementById("info_1").getElementsByTag("tr");
        Courses courses = new Courses();
        entries.subList(2, entries.size()).forEach(entry -> {
            CourseData courseData = CourseData.from(entry);
            courses.put(courseData.getName(), courseData);
        });

        return courses;
    }
}
