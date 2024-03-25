package crawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;

import com.google.common.base.Charsets;

public class FileManager {
    public static final Lock lock = new ReentrantLock();

    private static HashSet<Path> downloadedResources = new HashSet<Path>();

    private static String _dstDir = "";

    public static void initDstDir(String dstDir) {
        if (!_dstDir.equals("")) {
            System.out.println("DstDir is already initialized.");
            return;
        }
        _dstDir = dstDir;
        if (!_dstDir.endsWith("/")) {
            _dstDir += "/";
        }
    }

    public static String getDstDir() {
        return _dstDir;
    }

    /**
     * ファイルの存在を確認し、存在ない場合には新規に登録する
     * @param path
     * @return ファイルが存在するならば、Trueを返す
     */
    public static boolean checkExistenceThenRegister(Path path) {
        lock.lock();
        if (downloadedResources.contains(path)) {
            lock.unlock();
            return true;
        }
        downloadedResources.add(path);
        lock.unlock();
        return false;
    }

    private static void createFile(Path path) throws IOException {
        File file = path.toFile();
        File parent = file.getParentFile();
        boolean isExists = file.exists();
        if (!isExists) {
            parent.mkdirs();
            file.createNewFile();
        }
    }

    public static void save(Path path, Document doc) {
        try {
            createFile(path);
            FileWriter fileWriter = new FileWriter(path.toFile(), Charsets.UTF_8);
            fileWriter.write(doc.html());
            fileWriter.close();
        } catch (IOException e) {
            System.out.println(path.toString());
            e.printStackTrace();
            // System.exit(1);
        }
    }

    public static void save(Path path, Response res) {
        try {
            createFile(path);
            // System.out.println(String.format("Saving %s", file.getPath()));
            FileOutputStream outputStream = new FileOutputStream(path.toFile());
            outputStream.write(res.bodyAsBytes());
            outputStream.close();
        } catch (IOException e) {
            System.out.println(path.toString());
            e.printStackTrace();
            // System.exit(1);
        }
    }
}
