package crawler;

import java.nio.file.Path;

import org.jsoup.nodes.Node;
import org.jsoup.select.NodeVisitor;

class PageNodeVisiter implements NodeVisitor {
    private final int currentDepth;
    private final String baseURL;

    public PageNodeVisiter(int currentDepth, String baseURL) {
        this.currentDepth = currentDepth;
        this.baseURL = baseURL;
    }

    @Override
    public void head(Node node, int depth) {
        localizeAttr(node, "src");
        localizeAttr(node, "srcset");
        if (node.normalName().equals("a")) {
            if (currentDepth > 1) {
                localizeAttr(node, "href");
            }
        } else {
            localizeAttr(node, "href");
        }
    }

    private void localizeAttr(Node node, String attrName) {
        String targetURL = node.attr(attrName);
        if (URLUtil.isURL(targetURL)) {
            targetURL = URLUtil.formatURL(targetURL, baseURL);
            Path dstFilePath = URLUtil.convertURLtoPath(targetURL);
            // 初出のファイルのみをダウンロードする
            if (!FileManager.checkExistenceThenRegister(dstFilePath)) {
                Downloader.download(targetURL, dstFilePath, currentDepth - 1);
            }
            // attributeをダウンロードしたファイルへのパスに置き換える
            node.attr(attrName, dstFilePath.toString());
        }
    }
}
