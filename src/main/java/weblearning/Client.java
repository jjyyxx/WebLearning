package weblearning;

import common.AuthException;
import common.SingleHostHttpClient;
import okhttp3.HttpUrl;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public abstract class Client extends SingleHostHttpClient {
    protected Client(String host) {
        super(host);
    }

    Response getSync(HttpUrl url) throws IOException, AuthException {
        Response response = get(url);
        checkAuth(response);
        return response;
    }

    public CompletableFuture<Response> getRawAsync(HttpUrl url) {
        return super.getBaseAsync(url).thenCompose(this::wrapResponse);
    }

    public CompletableFuture<Document> getAsync(HttpUrl url) {
        return super.getBaseAsync(url).thenCompose(this::wrapDocument);
    }

    Response postSync(HttpUrl url, RequestBody requestBody) throws IOException, AuthException {
        Response response = post(url, requestBody);
        checkAuth(response);
        return response;
    }

    public CompletableFuture<Response> postRawAsync(HttpUrl url, RequestBody requestBody) {
        return super.postBaseAsync(url, requestBody).thenCompose(this::wrapResponse);
    }

    CompletableFuture<Document> postAsync(HttpUrl url, RequestBody requestBody) {
        return super.postBaseAsync(url, requestBody).thenCompose(this::wrapDocument);
    }

    protected abstract void checkAuth(Response response) throws AuthException;

    private CompletableFuture<Response> wrapResponse(Response response) {
        CompletableFuture<Response> future = new CompletableFuture<>();

        try {
            checkAuth(response);
            future.complete(response);
        } catch (AuthException e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    private CompletableFuture<Document> wrapDocument(Response response) {
        CompletableFuture<Document> future = new CompletableFuture<>();

        try {
            checkAuth(response);
            future.complete(Jsoup.parse(response.body().byteStream(), "UTF-8", response.request().url().toString()));
        } catch (AuthException | IOException e) {
            future.completeExceptionally(e);
        }
        return future;
    }
}
