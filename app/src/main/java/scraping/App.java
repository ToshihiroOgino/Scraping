package scraping;

import java.nio.file.Paths;

public class App {
    private static void parseYahoo() {
        String targetURL = "https://www.yahoo.co.jp/";
        String targetDir = Paths.get("./out").toAbsolutePath().toString();
        Parser parser = new Parser(targetURL, targetDir);
        parser.execute();
    }

    public static void main(String[] args) {
        parseYahoo();
    }
}
