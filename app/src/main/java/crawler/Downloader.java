package crawler;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.file.Path;

import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;

import org.jsoup.Jsoup;

class Downloader {
    /** 絶対URLのみ指定可能 */
    public static void download(String targetURL, Path dstFilePath, int depthLeft) {
        // depthLeftが0以下のHTMLは保存しない
        if (depthLeft <= 0) {
            if ((dstFilePath.getFileName().endsWith(".html") ||
                    dstFilePath.getFileName().endsWith(".ashx") ||
                    dstFilePath.getFileName().endsWith(".asp"))) {
                return;
            }
        }

        System.out.println(String.format("Downloading %s -> %s", targetURL, dstFilePath.toString()));

        // コンテンツタイプを取得するために先にGETリクエストを送信する
        Response response = null;
        try {
            response = Jsoup.connect(targetURL).ignoreContentType(true).execute();
        } catch (SocketTimeoutException e) {
            // System.out.println(String.format("Timeout: %s", targetURL));
            return;
        } catch (IOException | IllegalArgumentException e) {
            // System.out.println("Error targetURL: " + targetURL);
            return;
        }

        String contentType = response.contentType();
        boolean isHTML = (contentType != null && contentType.contains("text/html"));

        // URL先のファイルを保存する
        if (isHTML) {
            if (depthLeft <= 0) {
                // この関数の冒頭で弾いた拡張子以外のHTMLを見つけた場合にはログとして出力する
                if (!targetURL.endsWith("/")) {
                    System.out.println(String.format("Skipped page unknown ext that are too deep. (%s)", targetURL));
                }
                return;
            }
            Document doc = WebPageLocalizer.localize(response, targetURL, FileManager.getDstDir(), depthLeft - 1);
            FileManager.save(dstFilePath, doc);
        } else {
            FileManager.save(dstFilePath, response);
        }

        System.out.println(String.format("Complete download! (%s)", targetURL));
    }
}
