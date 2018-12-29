package weblearning;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import common.Navigable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import okhttp3.HttpUrl;

public abstract class FileEntry extends RecursiveTreeObject<FileEntry> implements Navigable {
    public final StringProperty title = new SimpleStringProperty();
    public final StringProperty description = new SimpleStringProperty();
    public final StringProperty size = new SimpleStringProperty();
    public final StringProperty uploadTime = new SimpleStringProperty();
    public final StringProperty isRead = new SimpleStringProperty();
    protected String args;

    @Override public abstract HttpUrl getURL();
}
