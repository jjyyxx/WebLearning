package weblearning;

import okhttp3.MediaType;

import java.io.File;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {
    public static MediaType probeMediaType(File file) {
        MediaType mediaType;
        try {
            mediaType = MediaType.parse(Files.probeContentType(file.toPath()));
        } catch (Exception e) {
            mediaType = MediaType.parse("application/octet-stream");
        }
        return mediaType;
    }

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static String toSimpleDate(long date) {
        return FORMAT.format(new Date(date));
    }

}
