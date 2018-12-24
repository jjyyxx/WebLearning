package background;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

class DownloadInfo {
    public final InputStream inputStream;
    public final Path dir;
    public final String name;
    public final String ext;
    public final Path path;

    public DownloadInfo(Path dir, String name, String ext, InputStream inputStream) {
        this.dir = dir;
        this.name = name;
        this.ext = ext;
        this.inputStream = inputStream;
        this.path = getSafePath();
    }

    public Path getSafePath() {
        Path path = dir.resolve(name + ext);
        int x = 0;
        while (Files.exists(path)) {
            path = dir.resolve(name + "(" + ++x + ")" + ext);
        }
        return path;
    }
}
