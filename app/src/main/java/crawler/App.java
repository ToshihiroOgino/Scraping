package crawler;

public class App {
    private static void parseYahoo() {
        final String targetURL = "https://www.yahoo.co.jp/";
        final String targetDir = "./out";
        Crawler crawler = new Crawler(targetURL, targetDir);
        crawler.execute();
    }

    public static void main(String[] args) {
        parseYahoo();
    }
}
