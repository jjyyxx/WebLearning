package weblearning;

import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static java.awt.Desktop.getDesktop;
import static java.awt.Desktop.isDesktopSupported;

public class Resource {
    private final String url;
    private final String title;
    private final String description;

    Resource(String url, String title, String description) {
        this.url = url;
        this.title = title;
        this.description = description;
    }

    public void browse() {
        if (isDesktopSupported()) {
            try {
                getDesktop().browse(new URI(url));
            } catch (URISyntaxException | IOException ignored) {}
        }
    }

    public static Resource from(Element entry) {
        org.jsoup.nodes.Element link = entry.child(0).child(0);
        String href = link.attr("href");
        String title = link.text();
        String description = entry.child(1).text();
        return new Resource(href, title, description);
    }
}
