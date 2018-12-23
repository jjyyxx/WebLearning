package common;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class Settings implements Serializable {
    private static final long serialVersionUID = 6425342;
    private static final Path STARTUP = Paths.get(System.getProperty("user.home"), "AppData/Roaming/Microsoft/Windows/Start Menu/Programs/Startup/weblearning.bat");
    private static final byte[] STARTBAT = ("javaw -Xmx500m -jar " + Settings.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getBytes();

    public static final Settings INSTANCE;

    public transient BooleanProperty autologin;
    public transient BooleanProperty autostart;
    public transient BooleanProperty separateByCourse;
    public PathRegistry pathRegistry;

    private Settings() {
        autologin = new SimpleBooleanProperty(true);
        autostart = new SimpleBooleanProperty(false);
        separateByCourse = new SimpleBooleanProperty(false);
        pathRegistry = new PathRegistry();
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeBoolean(autologin.get());
        s.writeBoolean(autostart.get());
        s.writeBoolean(separateByCourse.get());
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        autologin = new SimpleBooleanProperty(s.readBoolean());
        autostart = new SimpleBooleanProperty(s.readBoolean());
        separateByCourse = new SimpleBooleanProperty(s.readBoolean());
    }

    static {
        Settings settings = DataStore.getObj("settings");
        INSTANCE = Objects.requireNonNullElseGet(settings, Settings::new);
        INSTANCE.autostart.addListener((o, oV, nV) -> {
            try {
                if (nV) {
                    Files.write(STARTUP, STARTBAT);
                } else {
                    Files.deleteIfExists(STARTUP);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> DataStore.putObj("settings", INSTANCE)));
    }
}
