package Common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

public class DataStore {
    private static final String AppName = "WebLearningClient";
    private static Path workingDir;
    private static Map<String, String> config = new HashMap<>();

    private static final Preferences prefs = Preferences.userNodeForPackage(DataStore.class);

    private static final String test;

    static {
        test = "";
        String dataDir = System.getenv("AppData");
        if (dataDir == null) {
            dataDir = System.getProperty("user.home");
        }
        workingDir = Paths.get(dataDir, AppName);
        if (!Files.exists(workingDir)) {
            try {
                Files.createDirectory(workingDir);
            } catch (IOException ignored) {}
        }
        try {
            stringToConfig(Files.readAllLines(workingDir.resolve("app.cfg")));
        } catch (IOException ignored) {}
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Files.write(workingDir.resolve("app.cfg"), configToString().getBytes());
            } catch (IOException ignored) {}
        }));
    }

    public static void put(String key, String value) {
        config.put(key, value);
    }

    public static String get(String key) {
        return config.get(key);
    }

    public static String get(String key, String defaultValue) {
        return config.getOrDefault(key, defaultValue);
    }

    private static String configToString() {
        StringBuilder builder = new StringBuilder();
        config.forEach((s1, s2) -> {
            builder.append(s1).append(":::").append(s2).append("\n");
        });
        return builder.toString();
    }

    private static void stringToConfig(List<String> entries) {
        for (String entry : entries) {
            String[] kv = entry.split(":::");
            if (kv.length == 2) {
                config.put(kv[0], kv[1]);
            }
        }
    }
}
