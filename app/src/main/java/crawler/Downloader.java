package crawler;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;

import org.jsoup.Jsoup;

class Downloader {
    private Response response;

    private boolean isHTML = false;

    private final String targetURL;
    private final String dstDir;
    private final int depthLeft;
    private final Path dstFilePath;

    public Path getDstFilePath() {
        return dstFilePath;
    }

    private static Queue<CompletableFuture<Void>> futures = new ArrayDeque<CompletableFuture<Void>>();

    public void waitDownload() {
        while (!futures.isEmpty()) {
            var fut = futures.poll();
            if (fut != null) {
                fut.join();
            }
        }
    }

    /**
     * @param targetURL 絶対URLのみ可
     * @param dstDir
     * @param depthLeft
     */
    public Downloader(String targetURL, String dstDir, int depthLeft) {
        this.targetURL = targetURL;
        this.dstDir = dstDir;
        this.depthLeft = depthLeft;

        // ダウンロード先のファイルパスをURLから決定する
        this.dstFilePath = Path.of(dstDir + URLUtil.convertURLtoPathStr(targetURL));
    }

    /**
     * @param baseURL 絶対URLのみ可
     * @param targetURL 相対URLでも可
     * @param dstDir
     * @param depthLeft
     */
    public Downloader(String baseURL, String targetURL, String dstDir, int depthLeft) {
        this.targetURL = URLUtil.formatURL(targetURL, baseURL);
        this.dstDir = dstDir;
        this.depthLeft = depthLeft;

        // ダウンロード先のファイルパスをURLから決定する
        String dstPathStr = dstDir + URLUtil.convertURLtoPathStr(targetURL);
        this.dstFilePath = Path.of(dstPathStr);
    }

    public void execute() {
        futures.add(CompletableFuture.runAsync(() -> {
            download();
        }));
    }

    private void download() {
        {
            // depthLeftが0以下のHTMLは保存しない
            String str = this.dstFilePath.toString();
            if ((str.endsWith(".html") || str.endsWith(".ashx") || str.endsWith(".asp"))
                    && depthLeft <= 0) {
                return;
            }
        }

        // とりあえず保存先にファイルを作成し、ダウンロードが実行されたことをファイルとして記録しておく
        boolean alreadyExists = FileManager.checkThenCreateFile(dstFilePath);

        if (alreadyExists) {
            // すでに同じURLがダウンロードされている場合には、ダウンロードを行わない
            return;
        }

        System.out.println(String.format("Downloading %s -> %s", targetURL, dstFilePath.toString()));

        // コンテンツタイプを取得するために先にGETリクエストを送信する
        try {
            this.response = Jsoup.connect(targetURL).ignoreContentType(true).execute();
        } catch (SocketTimeoutException e) {
            // System.out.println(String.format("Timeout: %s", targetURL));
            return;
        } catch (IOException | IllegalArgumentException e) {
            // System.out.println("Error targetURL: " + targetURL);
            return;
        }

        String contentType = this.response.contentType();
        if (contentType != null && contentType.contains("text/html")) {
            isHTML = true;
        }

        // URL先のファイルを保存する
        if (this.isHTML) {
            if (depthLeft <= 0) {
                // この関数の冒頭で弾いた拡張子以外のHTMLを見つけた場合にはログとして出力する
                System.out.println(String.format("Skipped page unknown ext that are too deep. (%s)", targetURL));
                return;
            }
            Document doc = WebPageLocalizer.localize(response, targetURL, dstDir, depthLeft);
            FileManager.save(dstFilePath, doc);
        } else {
            FileManager.save(dstFilePath, response);
        }

        System.out.println(String.format("Complete download! (%s)", targetURL));
    }
}
