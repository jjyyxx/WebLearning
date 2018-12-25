package weblearning;

import org.jsoup.nodes.Element;

import java.util.function.BiFunction;

/**
 * 课程信息对应对象
 */
public class Information {
    public String number;
    public String index;
    public String name;
    public String credit;
    public String classHour;
    public String group;
    public String classPresident;
    public Teacher teacher;
    public String teachingMaterial;
    public String referenceBooks;
    public String assessmentMethod;
    public String teachingMethod;
    public String introduction;
    public String calendar;

    static Information from(Element element) {
        Information info = new Information();
        BiFunction<Integer, Integer, String> getCellText = (row, col) -> element.child(row).child(col).text();
        info.number = getCellText.apply(0, 1);
        info.index = getCellText.apply(0, 3);
        info.name = getCellText.apply(1, 1);
        info.credit = getCellText.apply(2, 1);
        info.classHour = getCellText.apply(2, 3);
        info.group = getCellText.apply(3, 1);
        info.classPresident = getCellText.apply(3, 3);
        info.teacher = new Teacher(
                getCellText.apply(4, 2),
                getCellText.apply(4, 4),
                getCellText.apply(5, 1),
                getCellText.apply(6, 1)
        );
        info.teachingMaterial = getCellText.apply(7, 1);
        info.referenceBooks = getCellText.apply(8, 1);
        info.assessmentMethod = getCellText.apply(9, 1);
        info.teachingMethod = getCellText.apply(10, 1);
        info.introduction = getCellText.apply(11, 1);
        info.calendar = getCellText.apply(12, 1);
        return info;
    }
}

/**
 * 教师信息对应对象
 */
class Teacher {
    private final String name;
    private final String mail;
    private final String phone;
    private final String introduction;

    Teacher(String name, String mail, String phone, String introduction) {
        this.name = name;
        this.mail = mail;
        this.phone = phone;
        this.introduction = introduction;
    }
}