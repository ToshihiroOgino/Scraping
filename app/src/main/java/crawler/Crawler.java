package crawler;

import java.nio.file.Paths;

public class Crawler {
    private String targetURL;
    private String targetDirPath;
    private int maxDepth = 1;

    public Crawler() {
    }

    public void setTargetURL(String url) {
        this.targetURL = url;
    }

    public void setDirPath(String targetDirPath) {
        // 出力先のパスを整形する

        // 絶対パスに変換する
        targetDirPath = Paths.get(targetDirPath).toAbsolutePath().toString();

        // \と/./を/に変換する
        targetDirPath = targetDirPath.replace("\\", "/");
        targetDirPath = targetDirPath.replace("/./", "/");

        // 末尾を"/"にする
        if (!targetDirPath.endsWith("/")) {
            targetDirPath += "/";
        }

        this.targetDirPath = targetDirPath;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public void execute() {
        if (targetDirPath == null || targetDirPath == null || maxDepth <= 0) {
            System.out.println("More than one arguments is null.");
            return;
        }

        Downloader downloader = new Downloader(this.targetURL, this.targetDirPath, this.maxDepth);
        downloader.execute();
        downloader.waitDownload();
        System.out.println(String.format("All download was completed. (%s)", downloader.getDstFilePath().toString()));
    }
}
