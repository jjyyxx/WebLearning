package common;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;

public class Settings implements Serializable {
    private static final long serialVersionUID = 45115345;
    public static final Settings INSTANCE;

    public transient BooleanProperty autologin;
    public transient BooleanProperty autostart;

    private Settings() {
        autologin = new SimpleBooleanProperty(true);
        autostart = new SimpleBooleanProperty(false);
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeBoolean(autologin.get());
        s.writeBoolean(autostart.get());
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        autologin = new SimpleBooleanProperty(s.readBoolean());
        autostart = new SimpleBooleanProperty(s.readBoolean());
    }

    static {
        Settings settings = DataStore.getObj("settings");
        INSTANCE = Objects.requireNonNullElseGet(settings, Settings::new);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> DataStore.putObj("settings", INSTANCE)));
    }
}
