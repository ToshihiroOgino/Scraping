package crawler;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;

import org.jsoup.Connection.Response;

import org.jsoup.Jsoup;

record DownloaderDto(List<String> requirements, int depthLeft) {
}

class Downloader {
    private static int maxPermits = -1;
    private static Semaphore semaphore = null;
    private static Queue<CompletableFuture<DownloaderDto>> futures = new ArrayDeque<CompletableFuture<DownloaderDto>>();

    public static void initSemaphore(int permits) {
        if (permits > 0) {
            maxPermits = permits;
            semaphore = new Semaphore(permits);
        }
    }

    public static void waitAllDownloads() {
        while (!futures.isEmpty()) {
            var fut = futures.poll();
            DownloaderDto dto = fut.join();
            if (dto.requirements() == null) {
                continue;
            }
            int depthLeft = dto.depthLeft();
            dto.requirements().forEach(url -> {
                Path path = URLUtil.convertURLtoPath(FileManager.getDstDir() + url);
                Downloader.download(url, path, depthLeft);
            });
        }

        if (semaphore != null) {
            if (semaphore.availablePermits() != maxPermits) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
                waitAllDownloads();
            }
        }
    }

    /** 絶対URLのみ指定可能 */
    public static void download(final String targetURL, final Path dstFilePath, final int depthLeft) {
        if (depthLeft < 0) {
            return;
        }

        if (semaphore != null) {
            try {
                semaphore.acquire();
                futures.add(CompletableFuture.supplyAsync(() -> {
                    List<String> requirements = syncedDownload(targetURL, dstFilePath, depthLeft);
                    semaphore.release();
                    return new DownloaderDto(requirements, depthLeft - 1);
                }));
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }
        } else {
            futures.add(CompletableFuture.supplyAsync(() -> {
                return new DownloaderDto(syncedDownload(targetURL, dstFilePath, depthLeft), depthLeft - 1);
            }));
        }
    }

    private static List<String> syncedDownload(String targetURL, Path dstFilePath, int depthLeft) {
        // depthLeftが0以下のHTMLは保存しない
        // if (depthLeft <= 0) {
        //     if ((dstFilePath.getFileName().endsWith(".html") ||
        //             dstFilePath.getFileName().endsWith(".ashx") ||
        //             dstFilePath.getFileName().endsWith(".asp"))) {
        //         return;
        //     }
        // }

        System.out.println(String.format("Downloading %s -> %s", targetURL, dstFilePath.toString()));

        // コンテンツタイプを取得するために先にGETリクエストを送信する
        Response response = null;
        try {
            response = Jsoup.connect(targetURL).ignoreContentType(true).timeout(10 * 1000).execute();
        } catch (SocketTimeoutException e) {
            // System.out.println(String.format("Timeout: %s", targetURL));
            return null;
        } catch (IOException | IllegalArgumentException e) {
            // System.out.println("Error targetURL: " + targetURL);
            return null;
        }

        String contentType = response.contentType();
        boolean isHTML = (contentType != null && contentType.contains("text/html"));

        List<String> requirements = null;

        // URL先のファイルを保存する
        if (isHTML) {
            // if (depthLeft <= 0) {
            // この関数の冒頭で弾いた拡張子以外のHTMLを見つけた場合にはログとして出力する
            // if (!targetURL.endsWith("/")) {
            //     System.out.println(String.format("Skipped page unknown ext that are too deep. (%s)", targetURL));
            // }
            //     return;
            // }
            requirements = WebPageLocalizer.localizeAndSave(response, targetURL, dstFilePath, depthLeft);
        } else {
            FileManager.save(dstFilePath, response);
        }

        System.out.println(String.format("Complete download: %s", targetURL));

        return requirements;
    }
}
