package common;

import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SingleHostHttpClient {
    private OkHttpClient client;
    private String host;

    static {
        Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);
    }

    protected SingleHostHttpClient(String host) {
        client = new OkHttpClient.Builder()
                .cookieJar(new CookieJar() {
                    Map<String, Cookie> cookieMap = new HashMap<>();
                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        for (Cookie cookie : cookies) {
                            cookieMap.put(cookie.name(), cookie);
                        }
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        return new ArrayList<>(cookieMap.values());
                    }
                })
                .build();
        this.host = host;
    }

    public HttpUrl makeUrl(String path) {
        return new HttpUrl.Builder()
                .scheme("http")
                .host(host)
                .addEncodedPathSegments(path)
                .build();
    }

    public HttpUrl makeUrl(String path, String query) {
        return new HttpUrl.Builder()
                .scheme("http")
                .host(host)
                .addEncodedPathSegments(path)
                .encodedQuery(query)
                .build();
    }

    protected Response get(HttpUrl url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        return client.newCall(request).execute();
    }

    protected CompletableFuture<Response> getBaseAsync(HttpUrl url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        ResponseFuture responseFuture = new ResponseFuture();
        client.newCall(request).enqueue(responseFuture);
        return responseFuture.future;
    }

    protected Response post(HttpUrl url, RequestBody requestBody) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        return client.newCall(request).execute();
    }

    protected CompletableFuture<Response> postBaseAsync(HttpUrl url, RequestBody requestBody) {
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        ResponseFuture responseFuture = new ResponseFuture();
        client.newCall(request).enqueue(responseFuture);
        return responseFuture.future;
    }
}
