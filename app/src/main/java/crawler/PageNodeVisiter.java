package crawler;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import org.jsoup.nodes.Node;
import org.jsoup.select.NodeVisitor;

class PageNodeVisiter implements NodeVisitor {
    private final String dstDir;
    private final int depthLeft;
    private final String baseURL;

    private Queue<CompletableFuture<Void>> futures = new ArrayDeque<CompletableFuture<Void>>();

    public PageNodeVisiter(String dstDir, int depthLeft, String baseURL) {
        this.dstDir = dstDir;
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
            String[] targetAttrs = { "src", "srcset", "href" };

            for (String attr : targetAttrs) {
                String targetURL = node.attr(attr);
                if (URLUtil.isURL(targetURL)) {
                    // src先のファイルをダウンロードし、attributeをダウンロードしたファイルへのパスに置き換える
                    Downloader downloader = new Downloader(baseURL, targetURL, dstDir, this.depthLeft - 1);
                    downloader.execute();
                    node.attr(attr, downloader.getDstFilePath().toAbsolutePath().toString());
                }
            }
        }));
    }
}
