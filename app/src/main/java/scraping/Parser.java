package scraping;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.ArrayDeque;
import java.util.Queue;

import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;

import com.google.common.base.Charsets;

public class Parser {
    private Document doc;
    private String targetDirPath;
    private String targetHTMLPath;
    private Queue<CompletableFuture<Void>> futures = new ArrayDeque<CompletableFuture<Void>>();

    /**
     * @param targetURL ダウンロードするWebページのURL
     * @param targetDirPath 出力ディレクトリのパス
     */
    public Parser(String targetURL, String targetDirPath) {
        try {
            this.doc = Jsoup.connect(targetURL).get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        initTarget(doc, targetDirPath);
    }

    /**
     * @param doc Parse対象のDocument
     * @param targetDirPath 出力ディレクトリのパス
     */
    public Parser(Document doc, String targetDirPath) {
        initTarget(doc, targetDirPath);
    }

    public void execute() {
        traverse(doc);

        // いずれかのダウンロードが失敗したとき、HTMLを出力しないようにするため、ここでダウンロード完了を待つ
        while (!futures.isEmpty()) {
            CompletableFuture<Void> f = futures.poll();
            f.join();
        }

        File file = new File(targetHTMLPath);
        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
            FileWriter fileWriter = new FileWriter(file, Charsets.UTF_8);
            fileWriter.write(doc.html());
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println(String.format("Complete!\n  -> %s", targetHTMLPath));
    }

    private void initTarget(Document targetDoc, String targetDirPath) {
        // 絶対パスに変換する
        targetDirPath = Paths.get(targetDirPath).toAbsolutePath().toString();
        targetDirPath.replace("\\", "/");
        // 末尾を"/"にする
        if (!targetDirPath.endsWith("/")) {
            targetDirPath += "/";
        }
        this.targetDirPath = targetDirPath;

        this.doc = targetDoc;
        if (doc == null) {
            this.targetHTMLPath = "";
            return;
        }

        String targetURI = convertURLtoPath(targetDoc.baseUri());
        if (!targetURI.endsWith(".html")) {
            // 拡張子".html"が末尾に無い場合、末尾の"/"を全て削除して拡張子をつける
            if (targetURI.endsWith("/")) {
                targetURI = targetURI.substring(0, targetURI.length() - 1);
            }
            targetURI += ".html";
        }
        this.targetHTMLPath = targetURI;
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
        // String pathString = urlString.replace(":", "").replace("//", "/");
        String pathString = urlString.replace(":", "").replaceAll("[/]+", "/");
        // URLパラーメタを取り除く
        int idx = pathString.indexOf("?");
        if (idx > 0) {
            pathString = pathString.substring(0, pathString.indexOf("?"));
        }
        return targetDirPath + pathString;
    }

    private void download(String url, String dstPath) {
        try {
            // すでにファイルが存在する場合には無視する
            File file = new File(dstPath);
            file.getParentFile().mkdirs();
            if (file.exists()) {
                System.out.println(String.format("Skip: %s", url));
                return;
            }
            file.createNewFile();

            // ディレクトリを作成し、URL先のファイルを保存する
            Response res = Jsoup.connect(url).ignoreContentType(true).execute();
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(res.bodyAsBytes());
            outputStream.close();
            System.out.println(String.format("Downloaded: %s\n  -> %s", url, dstPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
