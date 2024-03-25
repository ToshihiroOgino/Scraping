package crawler;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import org.jsoup.nodes.Node;
import org.jsoup.select.NodeVisitor;

class PageNodeVisiter implements NodeVisitor {
    private final int depthLeft;
    private final String baseURL;

    private Queue<CompletableFuture<Void>> futures = new ArrayDeque<CompletableFuture<Void>>();

    public PageNodeVisiter(int depthLeft, String baseURL) {
        this.depthLeft = depthLeft;
        this.baseURL = baseURL;
    }

    public void waitLocalize() {
        while (!futures.isEmpty()) {
            var fut = futures.poll();
            fut.join();
        }
    }

    @Override
    public void head(Node node, int depth) {
        futures.add(CompletableFuture.runAsync(() -> {
            checkNode(node);
        }));
    }

    private void checkNode(Node node) {
        String[] targetAttrs = { "src", "srcset", "href" };

        for (String attr : targetAttrs) {
            String targetURL = node.attr(attr);
            if (URLUtil.isURL(targetURL)) {
                targetURL = URLUtil.formatURL(targetURL, baseURL);
                Path dstFilePath = URLUtil.convertURLtoPath(targetURL);
                // 初出のファイルのみをダウンロードする
                if (!FileManager.checkExistenceThenRegister(dstFilePath)) {
                    Downloader.download(targetURL, dstFilePath, depthLeft);
                }
                // attributeをダウンロードしたファイルへのパスに置き換える
                node.attr(attr, dstFilePath.toString());
            }
        }
    }
}
