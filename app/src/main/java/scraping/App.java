package scraping;

public class App {
    private static void parseYahoo() {
        final String targetURL = "https://www.yahoo.co.jp/";
        final String targetDir = "./out";
        Parser parser = new Parser(targetURL, targetDir);
        parser.execute();
    }

    public static void main(String[] args) {
        parseYahoo();
    }
}
