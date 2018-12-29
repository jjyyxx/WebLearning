package background;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 存储一个下载项目的所有信息
 */
class DownloadInfo {
    /**
     * 输入流，用于写入文件
     */
    public final InputStream inputStream;
    /**
     * 文件下载路径
     */
    public final Path dir;
    /**
     * 文件名称
     */
    public final String name;
    /**
     * 文件后缀名
     */
    public final String ext;
    /**
     * 文件全名（名称+标号+后缀名）
     */
    public final Path path;

    public DownloadInfo(Path dir, String name, String ext, InputStream inputStream) {
        this.dir = dir;
        this.name = name;
        this.ext = ext;
        this.inputStream = inputStream;
        this.path = getSafePath();
    }

    /**
     * 判断是否有重名文件，如果有则加(x)
     * @return 不重名的文件名
     */
    public Path getSafePath() {
        Path path = dir.resolve(name + ext);
        int x = 0;
        while (Files.exists(path)) {
            path = dir.resolve(name + "(" + ++x + ")" + ext);
        }
        return path;
    }
}
