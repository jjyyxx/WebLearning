package weblearning.v_old;

import common.AuthException;
import okhttp3.Response;
import weblearning.Client;

import java.util.List;

/**
 * 网络学堂的请求客户端
 */
public class ClientOld extends Client {
    private static final String HOST = "learn.tsinghua.edu.cn";
    private static final String SetCookieHeader = "Set-Cookie";
    private static final String IdentityCookieName = "THNSV2COOKIE";

    private static final Client ourInstance = new ClientOld();

    public static Client getInstance() {
        return ourInstance;
    }

    private ClientOld() {
        super(HOST);
    }

    /**
     * 检查是否验证成功
     */
    @Override protected void checkAuth(Response response) throws AuthException {
        List<String> cookies = response.headers(SetCookieHeader);
        for (String cookie : cookies) {
            if (cookie.startsWith(IdentityCookieName)) {
                return;
            }
        }
        response.close();
        throw new AuthException();
    }

}
