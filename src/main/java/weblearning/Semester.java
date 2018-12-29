package weblearning;

/**
 * 学期类型信息
 */
public enum Semester {
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

