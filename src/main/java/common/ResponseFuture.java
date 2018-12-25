package common;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * 对回调的包装，允许链式调用，简化写法
 * @see CompletableFuture
 */
public class ResponseFuture implements Callback {
    final CompletableFuture<Response> future = new CompletableFuture<>();

    @Override public void onFailure(Call call, IOException e) {
        future.completeExceptionally(e);
    }

    @Override public void onResponse(Call call, Response response) {
        future.complete(response);
    }
}
