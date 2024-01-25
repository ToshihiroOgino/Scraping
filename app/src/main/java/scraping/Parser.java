package scraping;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.LinkedList;

import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;

import com.google.common.base.Charsets;

public class Parser {
    private Document doc;
    private String targetDirPath;
    private List<CompletableFuture<Void>> futures = new LinkedList<CompletableFuture<Void>>();

    /**
     * @param targetURL ダウンロードするWebページのURL
     * @param targetDirPath 出力ディレクトリの絶対パス
     */
    public Parser(String targetURL, String targetDirPath) {
        try {
            this.doc = Jsoup.connect(targetURL).get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.targetDirPath = targetDirPath.replace("\\", "/");
        // 末尾を"/"にする
        if (!this.targetDirPath.endsWith("/")) {
            this.targetDirPath += "/";
        }
    }

    /**
     * @param doc Parse対象のDocument
     * @param targetDirPath 出力ディレクトリの絶対パス
     */
    public Parser(Document doc, String targetDirPath) {
        this.doc = doc;
        this.targetDirPath = targetDirPath.replace("\\", "/");
        // 末尾を"/"にする
        if (!this.targetDirPath.endsWith("/")) {
            this.targetDirPath += "/";
        }
    }

    public void execute() {
        traverse(doc);

        String dst = convertURLtoPath(doc.baseUri());
        if (!dst.endsWith(".html")) {
            // 拡張子".html"が末尾に無い場合、末尾の"/"を全て削除して拡張子をつける
            while (dst.endsWith("/")) {
                dst = dst.substring(0, dst.length() - 1);
            }
            dst += ".html";
        }

        // いずれかのダウンロードが失敗したとき、HTMLを出力しないようにするため、ここでダウンロード完了を待つ
        for (var f : futures) {
            f.join();
        }

        File file = new File(dst);
        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
            FileWriter fileWriter = new FileWriter(file, Charsets.UTF_8);
            fileWriter.write(doc.html());
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println(String.format("Complete!\n  -> %s", dst));
    }

    private void traverse(Node node) {
        final String tagName = node.normalName();
        String targetAttr = null;

        // tagによってURLを取得するattributeが異なる
        if (tagName.equals("script") || tagName.equals("img")) {
            targetAttr = "src";
        } else if (tagName.equals("link")) {
            String rel = node.attr("rel");
            if (rel.equals("stylesheet") || rel.endsWith("icon")) {
                targetAttr = "href";
            }
        } else if (tagName.equals("meta")) {
            String property = node.attr("property");
            if (property.endsWith("image")) {
                targetAttr = "content";
            }
        } else if (tagName.equals("source")) {
            String type = node.attr("type");
            if (type.contains("image")) {
                targetAttr = "srcset";
            }
        }

        if (targetAttr != null) {
            String srcURL = node.attr(targetAttr);
            // "//"から始まるURLを修正する
            if (srcURL.startsWith("//")) {
                srcURL = srcURL.replaceFirst("//", "https://");
            }
            if (!srcURL.equals("")) {
                // src先のファイルをダウンロードし、attributeをダウンロードしたファイルへのパスに置き換える
                final String fSrcURL = srcURL;
                final String fDstPath = convertURLtoPath(srcURL);

                futures.add(CompletableFuture.runAsync(() -> {
                    download(fSrcURL, fDstPath);
                }));
                node.attr(targetAttr, fDstPath);
            }
        }

        for (Node child : node.childNodes()) {
            traverse(child);
        }
    }

    private String convertURLtoPath(String urlString) {
        // ファイルに使用できない":"と"http://"などに含まれる"//"を削除する
        String pathString = urlString.replace(":", "").replace("//", "/");
        // URLパラーメタを取り除く
        int idx = pathString.indexOf("?");
        if (idx > 0) {
            pathString = pathString.substring(0, pathString.indexOf("?"));
        }
        return targetDirPath + pathString;
    }

    private void download(String url, String dstPath) {
        try {
            // ディレクトリを作成し、URL先のファイルを保存する
            Response res = Jsoup.connect(url).ignoreContentType(true).execute();
            File file = new File(dstPath);
            file.getParentFile().mkdirs();
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(res.bodyAsBytes());
            outputStream.close();
            System.out.println(String.format("Downloaded: %s\n  -> %s", url, dstPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
