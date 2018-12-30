package weblearning;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import common.Navigable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import okhttp3.HttpUrl;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public abstract class Operation extends RecursiveTreeObject<Operation> implements Navigable {
    public final StringProperty title = new SimpleStringProperty();
    public final StringProperty effectiveDate = new SimpleStringProperty();
    public final StringProperty deadline = new SimpleStringProperty();
    public final StringProperty isHandedIn = new SimpleStringProperty();
    public final StringProperty size = new SimpleStringProperty();
    protected String args;
    protected String submitArgs;
    protected boolean submitDisabled = false;
    protected String reviewArgs;
    protected boolean reviewDisabled = false;
    // 作业详细信息相关内容
    protected boolean detailsResolved = false;
    protected String description;
    protected boolean attachmentExists = true;
    protected String attachmentName;
    protected String attachmentArgs;
    protected String submissionContent;
    protected boolean submissionAttachmentExists = true;
    protected String submissionAttachmentName;
    protected String submissionAttachmentArgs;
    // 作业评价相关内容
    protected boolean reviewResolved = false;
    protected String reviewer;
    protected String reviewTime;
    protected String score;
    protected String remark;
    protected boolean remarkAttachmentExists = true;
    protected String remarkAttachmentName;
    protected String remarkAttachmentArgs;

    public abstract CompletableFuture<Void> resolveDetail();

    public abstract HttpUrl getAttachmentUrl();

    public abstract CompletableFuture<Void> submit(String content, File file) throws IllegalArgumentException;

    public abstract CompletableFuture<Void> submit(String content) throws IllegalArgumentException;

    public abstract CompletableFuture<Void> deleteAttachment();

    public abstract CompletableFuture<Void> resolveReview();

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

    @Override public abstract HttpUrl getURL();
}
