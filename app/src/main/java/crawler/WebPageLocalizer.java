package crawler;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;

class WebPageLocalizer {
    public static List<String> localizeAndSave(Response response, String pageURL, Path dstFilePath, int currentDepth) {
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

        FileManager.save(dstFilePath, doc);

        return visiter.urls;
    }
}
