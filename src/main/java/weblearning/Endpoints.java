package weblearning;

import okhttp3.FormBody;
import okhttp3.Response;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Endpoints {
    private static final String AUTH = "MultiLanguage/lesson/teacher/loginteacher.jsp";
    private static final String PROFILE = "MultiLanguage/vspace/vspace_userinfo1.jsp";
    private static final String CURRICULUM = "MultiLanguage/lesson/student/MyCourse.jsp";
    private static final Client client = Client.getInstance();

    public static CompletableFuture<Courses> getCurriculum() {
        return client.getAsync(client.makeUrl(CURRICULUM, "language=cn"))
                .thenApply(Courses::from);
    }

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
