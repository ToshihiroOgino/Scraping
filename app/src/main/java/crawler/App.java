package crawler;

public class App {
    private static void parseYahoo() {
        final String targetURL = "https://www.yahoo.co.jp/";
        final String targetDir = "/home/toshihiro/Downloads/crawler";
        FileManager.initDstDir(targetDir);
        final int maxDepth = 2;

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
