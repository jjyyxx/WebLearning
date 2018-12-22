package common;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Settings implements Serializable {
    private static final long serialVersionUID = 4515345;
    public static final Settings INSTANCE;

    public transient BooleanProperty autologin;
    public transient BooleanProperty autostart;
    public transient BooleanProperty separateByCourse;
    public Map<String, Path> coursePathMap;

    private Settings() {
        autologin = new SimpleBooleanProperty(true);
        autostart = new SimpleBooleanProperty(false);
        separateByCourse = new SimpleBooleanProperty(false);
        coursePathMap = new HashMap<>();
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
        Runtime.getRuntime().addShutdownHook(new Thread(() -> DataStore.putObj("settings", INSTANCE)));
    }
}
