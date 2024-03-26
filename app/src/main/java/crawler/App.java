package crawler;

public class App {
    private static void parseYahoo() {
        final String targetURL = "https://www.yahoo.co.jp/";
        final String targetDir = "./out";
        final int maxDepth = 2;
        // ダウンロードの最大同時実行数
        final int maxPermits = 1024;

        // maxDepth = 2
        // maxPermits = 4
        // exec time -> 175 sec

        // maxDepth = 2
        // maxPermits = 1024
        // exec time -> 71 sec

        Downloader.initSemaphore(maxPermits);

        FileManager.initDstDir(targetDir);

        Crawler crawler = new Crawler();
        crawler.setTargetURL(targetURL);
        crawler.setDirPath(targetDir);
        crawler.setMaxDepth(maxDepth);

        crawler.execute();
    }

    public static void main(String[] args) {
        parseYahoo();
    }
}
