package crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;

public class CrawlerTest {
    @Test
    void crawlerCanConvertURL() {
        Method method;
        try {
            method = Crawler.class.getDeclaredMethod("convertURLtoPath", String.class);
            method.setAccessible(true);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
        Document nullDoc = null;
        try {
            String relativePath = "aaa";
            String absolutePath = Paths.get(relativePath).toAbsolutePath().toString();

            Crawler instance1 = new Crawler(nullDoc, relativePath);
            assertEquals(
                    (String) method.invoke(instance1,
                            "https://s.yimg.jp/images/edit/202401/1/aaa.jpg"),
                    absolutePath + "/https/s.yimg.jp/images/edit/202401/1/aaa.jpg");
            assertEquals(
                    (String) method.invoke(instance1, "a///b"),
                    absolutePath + "/a/b");
            assertEquals(
                    (String) method.invoke(instance1, "a////b"),
                    absolutePath + "/a/b");

            relativePath = "./a";
            absolutePath = Paths.get(relativePath).toAbsolutePath().toString();
            Crawler instance2 = new Crawler(nullDoc, relativePath);
            assertEquals(
                    (String) method.invoke(instance2, "b"),
                    absolutePath + "/b");

            Crawler instance3 = new Crawler(nullDoc, "./a/");
            assertEquals(
                    (String) method.invoke(instance3, "b"),
                    absolutePath + "/b");
            assertEquals(
                    (String) method.invoke(instance3, "https://aaa"),
                    absolutePath + "/https/aaa");

        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void parseMultiDupeImg() {
        try {
            // 同一ファイルを同時に複数ダウンロードする
            final String testHTML = "./src/test/resources/test.html";
            final String testOut = "./out_test";
            File file = Paths.get(testHTML).toFile();
            Document doc = Jsoup.parse(file);
            Crawler crawler = new Crawler(doc, testOut);
            crawler.execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
