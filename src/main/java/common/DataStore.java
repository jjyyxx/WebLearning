package common;

import com.sun.jna.platform.win32.WinCrypt;
import com.sun.jna.platform.win32.WinDef;
import platform.win.Crypt32;

import java.io.*;
import java.util.prefs.Preferences;

public class DataStore {
    public static final Preferences prefs = Preferences.userNodeForPackage(DataStore.class);
    private static final byte[] fakeDef = new byte[1];

    public static void putEncrypt(String key, String value) {
        WinCrypt.DATA_BLOB in = new WinCrypt.DATA_BLOB(value);
        WinCrypt.DATA_BLOB out = new WinCrypt.DATA_BLOB();
        if (Crypt32.CryptProtectData(in, null, null, null, null, new WinDef.DWORD(0), out)) {
            prefs.putByteArray(key, out.getData());
        } else {
            prefs.put(key, "");
        }
    }

    public static String getDecrypt(String key, String defaultValue) {
        WinCrypt.DATA_BLOB in = new WinCrypt.DATA_BLOB(prefs.getByteArray(key, fakeDef));
        WinCrypt.DATA_BLOB out = new WinCrypt.DATA_BLOB();
        if (Crypt32.CryptUnprotectData(in, null, null, null, null, new WinDef.DWORD(0), out)) {
            byte[] bytes = out.getData();
            return new String(bytes, 0, bytes.length - 1);
        } else {
            return defaultValue;
        }
    }

    public static void put(String key, String value) {
        prefs.put(key, value);
    }

    public static String get(String key, String defaultValue) {
        return prefs.get(key, defaultValue);
    }

    public static <T> T getObj(String key) {
        byte[] byteArray = prefs.getByteArray(key, null);
        if (byteArray == null) {
            return null;
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
        try (ObjectInput in = new ObjectInputStream(bis)) {
            return (T) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }

    public static <T> void putObj(String key, T obj) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(obj);
            out.flush();
            prefs.putByteArray(key, bos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
