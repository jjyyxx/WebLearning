package Common;

import org.jsoup.nodes.Element;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Util {
    public static String getArg(String url) {
        if (url.startsWith("javascript")) {
            url = url.split("'")[1];
        }
        return url.substring(url.indexOf('?') + 1);
    }

    public static boolean isDisabled(Element element) {
        if (element.hasAttr("disabled")) {
            String disabledString = element.attr("disabled");
            return disabledString.equals("") || disabledString.equals("true");
        } else {
            return false;
        }
    }

    public static <T, K, U> Collector<T, ?, Map<K,U>> toLinkedMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends U> valueMapper)
    {
        return Collectors.toMap(
                keyMapper,
                valueMapper,
                (u, v) -> {
                    throw new IllegalStateException(String.format("Duplicate key %s", u));
                },
                LinkedHashMap::new
        );
    }
}
