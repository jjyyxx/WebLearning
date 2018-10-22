package app.controls;

import javafx.scene.shape.SVGPath;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Logo extends SVGPath {
    private static final String LOGO;

    static {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        InputStream inputStream = Logo.class.getResourceAsStream("/app/logo.txt");
        try {
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
        } catch (IOException ignored) {}

        LOGO = result.toString();
    }

    public Logo() {
        setContent(LOGO);
    }
}
