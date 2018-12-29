package weblearning;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 学期具体信息
 */
public class SemesterData {
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

    public static SemesterData valueOf(String semester) {
        Matcher matcher = semesterPattern.matcher(semester);
        if (matcher.find()) {
            return new SemesterData(Integer.valueOf(matcher.group(1)), Semester.from(matcher.group(2)));
        } else {
            throw new IllegalArgumentException();
        }
    }
}
