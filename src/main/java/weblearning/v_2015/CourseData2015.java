package weblearning.v_2015;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.HttpUrl;
import okhttp3.Response;
import weblearning.*;
import weblearning.v_2015.message_model.BaseMessage;
import weblearning.v_2015.message_model.FileMessage;
import weblearning.v_2015.message_model.HomeworkMessage;
import weblearning.v_2015.message_model.NoticeMessage;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 仅提供基本支持，暂未进一步实现
 */
public class CourseData2015 extends CourseData {
    private static final Client client = Client2015.getInstance();
    private static final String BULLETIN = "http://learn.cic.tsinghua.edu.cn/b/myCourse/notice/listForStudent/%s?currentPage=1&pageSize=1000";
    private static final String FILE = "http://learn.cic.tsinghua.edu.cn/b/myCourse/tree/getCoursewareTreeData/%s/0";
    private static final String OPERATION = "http://learn.cic.tsinghua.edu.cn/b/myCourse/homework/list4Student/%s/0";
    private static final Gson gson = new Gson();
    private static final Type BulletinType = new TypeToken<BaseMessage<NoticeMessage>>() {}.getType();
    private static final Type FileType = new TypeToken<BaseMessage<Map<String, FileMessage>>>() {}.getType();
    private static final Type OperationType = new TypeToken<BaseMessage<List<HomeworkMessage>>>() {}.getType();

    public CourseData2015(String url, String name, String operations, String notices, String files) {
        super(name, Version.V_2015);
        id = url.substring(54);
        this.unsubmittedOperations.set(Integer.valueOf(operations));
        this.unreadBulletins.set(Integer.valueOf(notices));
        this.unreadFiles.set(Integer.valueOf(files));
    }

    @Override public CompletableFuture<Bulletin[]> resolveBulletins() {
        return client.getRawAsync(HttpUrl.get(String.format(BULLETIN, id))).thenApply(response -> {
            BaseMessage<NoticeMessage> message = toModel(response, BulletinType);
            List<NoticeMessage> noticeMessages = message.paginationList.recordList;
            if (noticeMessages.size() == 0) {
                return new Bulletin[]{};
            }
            Bulletin[] bulletins = new Bulletin[noticeMessages.size()];
            for (int i = 0; i < noticeMessages.size(); i++) {
                final Bulletin bulletin = Bulletin2015.from(noticeMessages.get(i));
                bulletins[i] = bulletin;
                if (!bulletin.isRead.get().equals(Bulletin2015.TRUE)) {
                    bulletin.isRead.addListener((o, oV, nV) -> unreadBulletins.set(unreadBulletins.get() - 1));
                }
            }
            return bulletins;
        });
    }

    @Override public CompletableFuture<Map<String, FileEntry[]>> resolveFileEntries() {
        return client.getRawAsync(HttpUrl.get(String.format(FILE, id))).thenApply(response -> {
            BaseMessage<Map<String, FileMessage>> message = toModel(response, FileType);
            Map<String, FileEntry[]> map = new LinkedHashMap<>();

            for (Map.Entry<String, FileMessage> stringFileMessageEntry : message.resultList.entrySet()) {
                for (Map.Entry<String, FileMessage.FileContainer> entry : stringFileMessageEntry.getValue().childMapData.entrySet()) {
                    List<BaseMessage.CourseCourseware> fileList = entry.getValue().courseCoursewareList;
                    FileEntry[] fileEntries = new FileEntry[fileList.size()];
                    for (int i = 0; i < fileList.size(); i++) {
                        FileEntry fileEntry = FileEntry2015.from(fileList.get(i));
                        fileEntries[i] = fileEntry;
                        if (!fileEntry.isRead.get().equals(FileEntry2015.TRUE)) {
                            fileEntry.isRead.addListener((o, oV, nV) -> unreadFiles.set(unreadFiles.get() - 1));
                        }
                    }
                    map.put(entry.getValue().courseOutlines.title, fileEntries);
                }
            }

            return map;
        });
    }

    @Override public CompletableFuture<Operation[]> resolveOperations() {
        return client.getRawAsync(HttpUrl.get(String.format(OPERATION, id))).thenApply(response -> {
            BaseMessage<List<HomeworkMessage>> message = toModel(response, OperationType);
            List<HomeworkMessage> resultList = message.resultList;
            if (resultList.size() == 0) {
                return new Operation[]{};
            }
            Operation[] operations = new Operation[resultList.size()];
            for (int i = 0; i < operations.length; i++) {
                final Operation operation = Operation2015.from(id, resultList.get(i));
                operations[i] = operation;
                if (!operation.isHandedIn.get().equals(Operation2015.TRUE)) {
                    operation.isHandedIn.addListener((o, oV, nV) -> unsubmittedOperations.set(unsubmittedOperations.get() - 1));
                }
            }
            return operations;
        });
    }

    @Override public CompletableFuture<Map<String, String>> resolveAllOperationScores() {
        return null;
    }

    public static  <T> T toModel(Response response, Type type) {
        try {
            return gson.fromJson(response.body().string(), type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override public String getUrl() {
        return "http://learn.cic.tsinghua.edu.cn/f/student/coursehome/" + id;
    }

    @Override public Client getClient() {
        return client;
    }
}
