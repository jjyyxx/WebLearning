package weblearning;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import common.Navigable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import okhttp3.HttpUrl;

import java.util.concurrent.CompletableFuture;

public abstract class Bulletin extends RecursiveTreeObject<Bulletin> implements Navigable {
    public final StringProperty name = new SimpleStringProperty();
    public final StringProperty publisher = new SimpleStringProperty();
    public final StringProperty time = new SimpleStringProperty();
    public final StringProperty isRead = new SimpleStringProperty();
    public final StringProperty content = new SimpleStringProperty();
    protected String args;
    public boolean contentResolved = false;

    public abstract CompletableFuture<StringProperty> resolveContent();

    public abstract CompletableFuture<Void> markAsRead();

    @Override public abstract HttpUrl getURL();
}
