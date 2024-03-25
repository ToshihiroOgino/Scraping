package crawler;

import java.math.BigInteger;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class URLUtil {
    /**
     * 文字列がURLかどうかを判別する
     */
    public static boolean isURL(String str) {
        if (str.startsWith("/") | str.startsWith("http"))
            return true;
        return false;
    }

    /**
     * targetURLを絶対URLに変換する
     */
    public static String formatURL(String targetURL, String parentURL) {
        if (targetURL.startsWith("http")) {
            // httpから始まるURLは正しいので何もしない
        } else if (targetURL.startsWith("//")) {
            targetURL = "https:" + targetURL;
        } else if (targetURL.startsWith("/")) {
            // 相対URLの場合、ホスト名を補完する
            String hostName = getHostName(parentURL);
            targetURL = "https://" + hostName + targetURL;
        } else {
            throw new RuntimeException(String.format("Detected incorrect URL.(%s)", targetURL));
        }
        return targetURL;
    }

    /**
     * 文字列からホスト名を取得する
     * @param url
     * @return
     */
    public static String getHostName(String url) {
        String host = url.replaceFirst("(?i)(http)(s)?(:\\/\\/)", "");
        int idx = host.indexOf("/");
        if (idx > 0) {
            host = host.substring(0, idx);
        }
        return host;
    }

    public static Path convertURLtoPath(String url) {
        // クエリを削除
        url = removeQuery(url);

        if (url.startsWith("http") || url.startsWith("//")) {
            // https://や//で始まるURLはそれらを削除する
            int idx = url.indexOf("//");
            url = url.substring(idx + 2);

            // ホスト名が末尾のURLには/を追加する
            String host = getHostName(url);
            if (url.endsWith(host)) {
                url += "/";
            }
        } else if (url.startsWith("/")) {
            // 相対URLの場合、冒頭の/を削除する
            url = url.substring(1);
        }

        // 拡張子を抽出
        final String ext = getURLExt(url);

        // urlの文字数が長すぎる場合にはハッシュ化して短縮する
        if (url.length() > 230) {
            try {
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                byte[] hash = md5.digest(url.getBytes());
                // 拡張子は残す
                url = String.format("longName/%020x%s", new BigInteger(hash), ext);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        if (url.endsWith("/")) {
            url += "index.html";
        } else if (ext == "") {
            url += "/index.html";
        } else if (url.endsWith(".ashx") || url.endsWith(".asp")) {
            // ASP NET系の拡張子をHTMLに変換する
            url += ".html";
        }

        Path path = Path.of(FileManager.getDstDir() + url);

        return path;
    }

    private static String removeQuery(String url) {
        int qIdx = url.indexOf("?");
        if (qIdx > 0) {
            url = url.substring(0, qIdx);
        }
        return url;
    }

    public static String getURLExt(String url) {
        url = removeQuery(url);

        // 拡張子を抽出
        String ext = "";
        if (url.matches("^.+\\.[A-z]+$")) {
            int idx = url.lastIndexOf(".");
            ext = url.substring(idx);
        }

        return ext;
    }
}
