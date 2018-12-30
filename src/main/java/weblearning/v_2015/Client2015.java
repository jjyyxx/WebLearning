package weblearning.v_2015;

import common.AuthException;
import okhttp3.HttpUrl;
import okhttp3.Response;
import weblearning.Client;

import java.util.concurrent.CompletableFuture;

/**
 * 网络学堂的请求客户端
 */
public class Client2015 extends Client {
    private static final String HOST = "http://learn.cic.tsinghua.edu.cn";
    private static final String LocationHeader = "Location";
    private static final String AuthFailedPage = "http://learn.cic.tsinghua.edu.cn/ce/502";

    private static final Client ourInstance = new Client2015();

    public static Client getInstance() {
        return ourInstance;
    }

    protected Client2015() {
        super(HOST);
    }

    public static CompletableFuture<Void> initialize(String roamingUrl) {
        return getInstance().getRawAsync(HttpUrl.get(roamingUrl)).thenAccept(Response::close);
    }

    /**
     * 检查是否验证成功
     */
    @Override protected void checkAuth(Response response) throws AuthException {
        if (response.isRedirect() && response.header(LocationHeader).equals(AuthFailedPage)) {
            throw new AuthException();
        }
    }

}
