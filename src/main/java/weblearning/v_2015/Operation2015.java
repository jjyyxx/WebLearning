package weblearning.v_2015;

import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import weblearning.Client;
import weblearning.Operation;
import weblearning.v_2015.message_model.BaseMessage;
import weblearning.v_2015.message_model.HomeworkMessage;
import weblearning.v_2015.message_model.IdMessage;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

/**
 * 单个作业对应的对象
 */
public class Operation2015 extends Operation {
    public static final String TRUE = "已经提交";
    private static final Client client = Client2015.getInstance();
    private static final HttpUrl STUID = HttpUrl.get("http://learn.cic.tsinghua.edu.cn/b/m/getStudentById");
    private static final String UPLOAD = "http://learn.cic.tsinghua.edu.cn/b/myCourse/homework/uploadFileStudentName/%s/%s";
    private static final String HANDIN = "http://learn.cic.tsinghua.edu.cn/b/myCourse/homeworkRecord/saveOrUpdate";

    private static final Type IdMessage = new TypeToken<BaseMessage<IdMessage>>(){}.getType();
    private static final Type UploadMessage = new TypeToken<BaseMessage<String>>(){}.getType();
    private final long workId;
    private final String courseId;

    public Operation2015(String title, String effectiveDate, String deadline, String isHandedIn, String size, String detail, long workId, String courseId) {
        this.workId = workId;
        this.courseId = courseId;
//        this.args = getArg(url);
        this.title.set(title);
        this.effectiveDate.set(effectiveDate);
        this.deadline.set(deadline);
        this.isHandedIn.set(isHandedIn);
        this.size.set(size);
        this.description = detail;
    }

    /**
     * 请求作业详细信息
     */
    @Override public CompletableFuture<Void> resolveDetail() {
        return CompletableFuture.completedFuture(null);
    }

    @Override public HttpUrl getAttachmentUrl() {
        return null;
    }

    /**
     * 含附件提交
     */
    @Override public CompletableFuture<Void> submit(String content, File file) throws IllegalArgumentException {
        return client.postRawAsync(STUID, RequestBody.create(null, new byte[0])).thenCompose(response -> {
            final BaseMessage<IdMessage> message = CourseData2015.toModel(response, IdMessage);
            final String id = message.dataSingle.id;
            MediaType mediaType;
            try {
                mediaType = MediaType.parse(Files.probeContentType(file.toPath()));
            } catch (IOException e) {
                mediaType = MediaType.parse("application/octet-stream");
            }
            return client.postRawAsync(HttpUrl.get(String.format(UPLOAD, courseId, id)), new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("uploadfile", file.getName(), RequestBody.create(mediaType, file)).build()).thenCompose(response1 -> {
                final BaseMessage<String> message1 = CourseData2015.toModel(response1, UploadMessage);
                final String fileId = message1.result;
                return client.postRawAsync(HttpUrl.get(HANDIN), new FormBody.Builder()
                        .add("resourcesMappingByHomewkAffix.fileId", fileId)
                        .add("homewkDetail", content)
                        .add("homewkId", String.valueOf(workId))
                        .add("status", "1").build());
            }).thenAccept(response1 -> {
                isHandedIn.set(TRUE);
                response.close();
            });
        });
    }

    /**
     * 无附件提交
     */
    @Override public CompletableFuture<Void> submit(String content) throws IllegalArgumentException {
        return client.postRawAsync(HttpUrl.get(HANDIN), new FormBody.Builder()
                .add("resourcesMappingByHomewkAffix.fileId", "")
                .add("homewkDetail", content)
                .add("homewkId", String.valueOf(workId))
                .add("status", "1").build())
                .thenAccept(response -> {
                    response.close();
                    isHandedIn.set(TRUE);
                });
    }

    /**
     * 删除作业附件
     */
    @Override public CompletableFuture<Void> deleteAttachment() {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * 获取作业评价
     */
    @Override public CompletableFuture<Void> resolveReview() {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * 从html元素解析作业对象
     *
     * @param entry 包含作业对象的html元素
     * @return 作业对象
     */
    public static Operation from(String courseId, HomeworkMessage entry) {
        final String title = entry.courseHomeworkInfo.title;
        final String effectiveDate = toSimpleDate(entry.courseHomeworkInfo.beginDate);
        final String deadline = toSimpleDate(entry.courseHomeworkInfo.endDate);
        final String state = entry.courseHomeworkRecord.status.equals("0") ? "尚未提交" : TRUE;
        final String size = state == TRUE ? entry.courseHomeworkRecord.resourcesMappingByHomewkAffix.fileSize : null;
        final String detail = entry.courseHomeworkInfo.detail;
        final long workId = entry.courseHomeworkInfo.homewkId;
        return new Operation2015(title, effectiveDate, deadline, state, size, detail, workId, courseId);
    }

    @Override public HttpUrl getURL() {
        return client.makeUrl("", args);
    }

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    static String toSimpleDate(long date) {
        return FORMAT.format(new Date(date));
    }
}
