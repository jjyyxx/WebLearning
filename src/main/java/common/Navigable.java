package common;

import okhttp3.HttpUrl;

/**
 * 表示可以在浏览器中浏览的通用接口
 */
public interface Navigable {
    HttpUrl getURL();
}
