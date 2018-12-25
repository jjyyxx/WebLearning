package common;

import org.jsoup.nodes.Element;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * 常用工具类
 */
public class Util {
    /**
     * 获取url中的参数部分
     */
    public static String getArg(String url) {
        if (url.startsWith("javascript")) {
            url = url.split("'")[1];
        }
        return url.substring(url.indexOf('?') + 1);
    }

    /**
     * 判断一个html元素是否处于disabled状态
     */
    public static boolean isDisabled(Element element) {
        if (element.hasAttr("disabled")) {
            String disabledString = element.attr("disabled");
            return disabledString.equals("") || disabledString.equals("true");
        } else {
            return false;
        }
    }

    /**
     * Stream转LinkedMap的辅助方法
     */
    public static <T, K, U> Collector<T, ?, Map<K, U>> toLinkedMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends U> valueMapper) {
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
