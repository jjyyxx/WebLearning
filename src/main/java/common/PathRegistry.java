package common;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 存储课程相关的下载路径信息的类
 */
public class PathRegistry implements Serializable {
    private static final long serialVersionUID = 3535246;
    public static final String DEFAULT = "DEFAULT";

    private final Map<String, String> coursePathMap;

    {
        coursePathMap = new HashMap<>();
        coursePathMap.putIfAbsent(DEFAULT, System.getProperty("user.home"));
    }

    /**
     * 按课程名获取路径，默认与DEFAULT相同
     */
    public Path get(String key) {
        String path = coursePathMap.get(key);
        if (path == null) {
            return Paths.get(coursePathMap.get(DEFAULT));
        } else {
            return Paths.get(path);
        }
    }

    public void put(String key, Path value) {
        if (value != null) {
            coursePathMap.put(key, value.toString());
        }
    }
}
