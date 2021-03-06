package weblearning;

import okhttp3.FormBody;
import okhttp3.Response;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import weblearning.v_2015.Client2015;
import weblearning.v_old.ClientOld;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 进行web请求的入口点
 */
public class Endpoints {
    private static final String AUTH = "MultiLanguage/lesson/teacher/loginteacher.jsp";
    private static final String PROFILE = "MultiLanguage/vspace/vspace_userinfo1.jsp";
    private static final String CURRICULUM = "MultiLanguage/lesson/student/MyCourse.jsp";
    private static final Client client = ClientOld.getInstance();

    /**
     * 获取全部课程
     */
    public static CompletableFuture<Courses> getCurriculum() {
        return client.getAsync(client.makeUrl(CURRICULUM, "language=cn"))
                .thenCompose(document -> Client2015.initialize(document.getElementsByTag("iframe").get(0).attr("src")).thenApply(v -> Courses.from(document)));
    }

    /**
     * 获取个人资料
     */
    public static CompletableFuture<Map<String, String>> getProfile() {
        return client.getAsync(client.makeUrl(PROFILE)).thenApply(document -> {
            Element tableBox = document.getElementById("table_box");
            Elements entries = tableBox.getElementsByTag("tr");
            entries.remove(entries.size() - 1);
            entries.remove(entries.size() - 1);
            LinkedHashMap<String, String> map = new LinkedHashMap<>();
            for (Element entry : entries) {
                map.put(entry.child(0).text(), entry.child(1).text());
            }
            return map;
        });
    }

    /**
     * 用户身份验证
     */
    public static CompletableFuture<Void> authenticate(String name, String pass) {
        return client.postRawAsync(client.makeUrl(AUTH),
                new FormBody.Builder()
                        .add("userid", name)
                        .add("userpass", pass)
                        .add("submit1", "登录")
                        .build())
                .thenAccept(Response::close);
    }
}
