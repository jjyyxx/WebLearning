package weblearning;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

enum Semester {
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

    public static Semester from(String semester) {
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
    private final int start;
    private final Semester type;
    private SemesterData(int start, Semester type) {
        this.start = start;
        this.type = type;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return start + 1;
    }

    public Semester getType() {
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
            return new SemesterData(Integer.valueOf(matcher.group(1)), Semester.from(matcher.group(2)));
        } else {
            throw new IllegalArgumentException();
        }
    }
}
