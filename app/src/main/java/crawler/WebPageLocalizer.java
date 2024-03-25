package crawler;

import java.io.IOException;

import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;

class WebPageLocalizer {
    public static Document localize(Response response, String pageURL, String dstDir, int currentDepth) {
        System.out.println(String.format("Localizing %s...", pageURL));

        Document doc;
        try {
            doc = response.parse();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        PageNodeVisiter visiter = new PageNodeVisiter(currentDepth, pageURL);
        doc.traverse(visiter);

        System.out.println(String.format("Complete localize: %s", pageURL));
        return doc;
    }
}
