package weblearning;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import common.Navigable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import okhttp3.*;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static common.Util.*;

public class Operation extends RecursiveTreeObject<Operation> implements Navigable {
    public static final String TRUE = "已经提交";
    private static final String DETAIL = "MultiLanguage/lesson/student/hom_wk_detail.jsp";
    private static final String SUBMIT = "MultiLanguage/lesson/student/hom_wk_submit.jsp";
    private static final String REVIEW = "MultiLanguage/lesson/student/hom_wk_view.jsp";
    private static final String DOWNLOAD = "uploadFile/downloadFile.jsp";
    private static final String UPLOAD = "uploadFile/uploadFile.jsp";
    private static final String HANDIN = "MultiLanguage/lesson/student/hom_wk_handin.jsp";
    private static final String DELETE = "/uploadFile/delFile_kczy.jsp";

    private static final Pattern DELETEPATTERN = Pattern.compile("onclick='check_del\\(\"(\\d+?)\",\".*?\",\"(.*?)\",\"(\\d+?)\"\\)'");
    private static final Client client = Client.getInstance();

    private String args;
    public final StringProperty title = new SimpleStringProperty();
    public final StringProperty effectiveDate = new SimpleStringProperty();
    public final StringProperty deadline = new SimpleStringProperty();
    public final StringProperty isHandedIn = new SimpleStringProperty();
    public final StringProperty size = new SimpleStringProperty();

    private String submitArgs;
    private boolean submitDisabled;
    private String reviewArgs;
    private boolean reviewDisabled;

    private boolean detailsResolved = false;
    private String description;
    private boolean attachmentExists = true;
    private String attachmentName;
    private String attachmentArgs;
    private String submissionContent;
    private boolean submissionAttachmentExists = true;
    private String submissionAttachmentName;
    private String submissionAttachmentArgs;

    private boolean reviewResolved = false;
    private String reviewer;
    private String reviewTime;
    private String score;
    private String remark;
    private boolean remarkAttachmentExists = true;
    private String remarkAttachmentName;
    private String remarkAttachmentArgs;

    public Operation(String url, String title, String effectiveDate, String deadline, String isHandedIn, String size, String submitUrl, boolean submitDisabled, String reviewUrl, boolean reviewDisabled) {
        this.args = getArg(url);
        this.title.set(title);
        this.effectiveDate.set(effectiveDate);
        this.deadline.set(deadline);
        this.isHandedIn.set(isHandedIn);
        this.size.set(size);
        this.submitArgs = getArg(submitUrl);
        this.submitDisabled = submitDisabled;
        this.reviewArgs = getArg(reviewUrl);
        this.reviewDisabled = reviewDisabled;
    }

    public CompletableFuture<Void> resolveDetail() {
        return client.getAsync(client.makeUrl(DETAIL, args)).thenAccept(document -> {
            Element table = document.getElementById("table_box").child(0);
            description = table.child(1).child(1).child(0).text();
            try {
                Element attachmentLink = table.child(2).child(1).child(0);
                attachmentName = attachmentLink.text();
                attachmentArgs = getArg(attachmentLink.attr("href"));
            } catch (IndexOutOfBoundsException ignored) {
                attachmentExists = false;
            }

            submissionContent = table.child(4).child(1).child(0).text();
            try {
                Element submissionLink = table.child(5).child(1).child(0);
                submissionAttachmentName = submissionLink.text();
                submissionAttachmentArgs = getArg(submissionLink.attr("href"));
            } catch (IndexOutOfBoundsException ignored) {
                submissionAttachmentExists = false;
            }

            detailsResolved = true;
        });
    }

    public HttpUrl getAttachmentUrl() {
       return client.makeUrl(DOWNLOAD, attachmentArgs);
    }

    public CompletableFuture<Void> submit(String content, File file) throws IllegalArgumentException {
        return submitDisabled ? CompletableFuture.completedFuture(null) :
                client.getAsync(client.makeUrl(SUBMIT, submitArgs)).thenCompose(document -> {
                    Element form = document.getElementById("F1");
                    Map<String, String> hiddenFieldsMap = form.getElementsByTag("input")
                            .stream()
                            .filter(element -> element.attr("type").equals("hidden"))
                            .collect(toLinkedMap(e -> e.attr("name"), e -> e.attr("value")));
                    String filename = file.getName();
                    Random random = new Random();
                    String rand = random.nextInt(10000) + "0" + random.nextInt(10000);
                    String newFileName = hiddenFieldsMap.get("newfilename") + rand + "_" + filename;
                    hiddenFieldsMap.put("post_homewk_link", newFileName);
                    hiddenFieldsMap.put("filename", newFileName);

                    MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                    hiddenFieldsMap.forEach(builder::addFormDataPart);
                    MediaType mediaType = null;
                    try {
                        mediaType = MediaType.parse(Files.probeContentType(file.toPath()));
                    } catch (IOException e) {
                        mediaType = MediaType.parse("application/octet-stream");
                    }
                    builder.addFormDataPart("post_rec_homewk_detail", content)
                            .addFormDataPart("upfile", file.getName(),
                                    RequestBody.create(mediaType, file))
                            .addFormDataPart("Submit", "提交");

                    return client.postRawAsync(client.makeUrl(UPLOAD), builder.build())
                            .thenCompose(response -> {
                                response.close();
                                FormBody.Builder formBuilder = new FormBody.Builder();
                                hiddenFieldsMap.forEach(formBuilder::add);
                                formBuilder.add("post_rec_homewk_detail", content).add("Submit", "提交").add("tj", "");
                                return client.postRawAsync(client.makeUrl(HANDIN), formBuilder.build());
                            }).thenAccept(response -> {
                                isHandedIn.set(TRUE);
                                response.close();
                            });
                });
    }

    public CompletableFuture<Void> submit(String content) throws IllegalArgumentException {
        return submitDisabled ? CompletableFuture.completedFuture(null) :
                client.getAsync(client.makeUrl(SUBMIT, submitArgs)).thenCompose(document -> {
                    Element form = document.getElementById("F1");
                    Map<String, String> hiddenFieldsMap = form.getElementsByTag("input")
                            .stream()
                            .filter(element -> element.attr("type").equals("hidden"))
                            .collect(toLinkedMap(e -> e.attr("name"), e -> e.attr("value")));
                    FormBody.Builder builder = new FormBody.Builder();
                    hiddenFieldsMap.forEach(builder::add);
                    builder.add("post_rec_homewk_detail", content).add("Submit", "提交").add("tj", "");
                    return client.postRawAsync(client.makeUrl(HANDIN), builder.build());
                }).thenAccept(response -> {
                    response.close();
                    isHandedIn.set(TRUE);
                });
    }

    public CompletableFuture<Void> deleteAttachment() {
        return client.getRawAsync(client.makeUrl(SUBMIT, submitArgs)).thenCompose(response -> {
            try (ResponseBody body = response.body()) {
                String content = body.string();
                Matcher matcher = DELETEPATTERN.matcher(content);
                if (!matcher.find()) {
                    return CompletableFuture.completedFuture(null);
                }
                return client.getRawAsync(client.makeUrl(DELETE, "course_id=" + URLEncoder.encode(matcher.group(3), StandardCharsets.UTF_8)
                        + "&file_id=" + URLEncoder.encode(matcher.group(0), StandardCharsets.UTF_8)
                        + "&filepath=" + URLEncoder.encode(matcher.group(2), StandardCharsets.UTF_8)
                        + "&backurl="))
                        .thenAccept(Response::close);
            } catch (IOException e) {
                return CompletableFuture.completedFuture(null);
            }
        });
    }

    public CompletableFuture<Void> resolveReview() {
        return reviewDisabled ? CompletableFuture.completedFuture(null) :
                client.getAsync(client.makeUrl(DETAIL, args)).thenAccept(document -> {
                    Element table = document.getElementById("table_box").child(0);
                    reviewer = table.child(4).child(1).text();
                    reviewTime = table.child(4).child(3).text();
                    score = table.child(5).child(1).text();
                    remark = table.child(6).child(1).child(0).text();
                    try {
                        Element remarkLink = table.child(7).child(1).child(0);
                        remarkAttachmentName = remarkLink.text();
                        remarkAttachmentArgs = getArg(remarkLink.attr("href"));
                    } catch (IndexOutOfBoundsException ignored) {
                        remarkAttachmentExists = false;
                    }
                });
    }

    public boolean isSubmitDisabled() {
        return submitDisabled;
    }

    public boolean isReviewDisabled() {
        return reviewDisabled;
    }

    public String getDescription() {
        return description;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public boolean isAttachmentExists() {
        return attachmentExists;
    }

    public static Operation from(Element entry) {
        Element link = entry.child(0).child(0);
        String href = link.attr("href");
        String title = link.text();
        String effectiveDate = entry.child(1).text();
        String deadline = entry.child(2).text();
        String state = entry.child(3).text();
        String size = entry.child(4).text();
        Element submitLink = entry.child(5).child(0);
        String submitUrl = submitLink.attr("onclick");
        boolean submitDisabled = isDisabled(submitLink);
        Element reviewLink = entry.child(5).child(1);
        String reviewUrl = reviewLink.attr("onclick");
        boolean reviewDisabled = isDisabled(reviewLink);
//        if (state.equals("尚未提交") && Notification.notifications.filtered(notificationObj -> notificationObj.title.equals(title)).size() == 0) {
//            Notification.addNotification(title, "", Date.from(LocalDateTime.parse(deadline).minusHours(6).atZone(ZoneId.systemDefault()).toInstant()), NotificationType.ASSIGNMENT);
//        }
        return new Operation(href, title, effectiveDate, deadline, state, size, submitUrl, submitDisabled, reviewUrl, reviewDisabled);
    }

    @Override public HttpUrl getURL() {
        return client.makeUrl(DETAIL, args);
    }
}
