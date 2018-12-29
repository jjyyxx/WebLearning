package weblearning;

import common.AuthException;
import common.SingleHostHttpClient;
import okhttp3.HttpUrl;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 网络学堂的请求客户端
 */
public class Client extends SingleHostHttpClient {
    private static final String HOST = "learn.tsinghua.edu.cn";
    private static final String SetCookieHeader = "Set-Cookie";
    private static final String IdentityCookieName = "THNSV2COOKIE";

    private static final Client ourInstance = new Client();

    public static Client getInstance() {
        return ourInstance;
    }

    private Client() {
        super(HOST);
    }

    Response getSync(HttpUrl url) throws IOException, AuthException {
        Response response = super.get(url);
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
        Response response = super.post(url, requestBody);
        checkAuth(response);
        return response;
    }

    public CompletableFuture<Response> postRawAsync(HttpUrl url, RequestBody requestBody) {
        return super.postBaseAsync(url, requestBody).thenCompose(this::wrapResponse);
    }

    CompletableFuture<Document> postAsync(HttpUrl url, RequestBody requestBody) {
        return super.postBaseAsync(url, requestBody).thenCompose(this::wrapDocument);
    }

    /**
     * 检查是否验证成功
     */
    private void checkAuth(Response response) throws AuthException {
        List<String> cookies = response.headers(SetCookieHeader);
        for (String cookie : cookies) {
            if (cookie.startsWith(IdentityCookieName)) {
                return;
            }
        }
        response.close();
        throw new AuthException();
    }

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
