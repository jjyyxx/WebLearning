package weblearning.v_2015;

import okhttp3.HttpUrl;
import weblearning.FileEntry;
import weblearning.Util;
import weblearning.v_2015.message_model.BaseMessage;

/**
 * 单个文件对应的对象
 */
public class FileEntry2015 extends FileEntry {
    public static final String TRUE = "已读";
    private static final String DOWNLOAD = "http://learn.cic.tsinghua.edu.cn/b/resource/downloadFileStream/";

    private FileEntry2015(String fileId, String title, String description, String size, String uploadTime, String state) {
        this.args = fileId;
        this.title.set(title);
        this.description.set(description);
        this.size.set(size);
        this.uploadTime.set(uploadTime);
        this.isRead.set(state);
    }

    /**
     * 从html元素解析文件对象
     * @param entry 包含文件对象的html元素
     * @return 文件对象
     */
    public static FileEntry from(BaseMessage.CourseCourseware entry) {
        String fileId = entry.resourcesMappingByFileId.fileId;
        String title = entry.title;
        String description = entry.detail;
        String size = entry.resourcesMappingByFileId.fileSize;
        String time = Util.toSimpleDate(entry.resourcesMappingByFileId.regDate);
        String state = "已读"; // ???
        return new FileEntry2015(fileId, title, description, size, time, state);
    }

    @Override public HttpUrl getURL() {
        return HttpUrl.get(DOWNLOAD+args);
    }
}
