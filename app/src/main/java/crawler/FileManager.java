package crawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;

import com.google.common.base.Charsets;

public class FileManager {
    public static final Lock lock = new ReentrantLock();

    /**
     * ファイルの存在を確認し、なければ作成する
     * @param path
     * @return ファイルが存在するならば、Trueを返す
     */
    public static boolean checkThenCreateFile(Path path) {
        File file = path.toFile();
        File parent = file.getParentFile();
        lock.lock();
        boolean isExists = file.exists();
        if (!isExists) {
            parent.mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println(path.toString());
                e.printStackTrace();
                System.exit(1);
            }
        }
        lock.unlock();
        return isExists;
    }

    public static void save(Path path, Document doc) {
        try {
            FileWriter fileWriter = new FileWriter(path.toFile(), Charsets.UTF_8);
            fileWriter.write(doc.html());
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            // System.exit(1);
            return;
        }
    }

    public static void save(Path path, Response res) {
        try {
            // System.out.println(String.format("Saving %s", file.getPath()));
            FileOutputStream outputStream = new FileOutputStream(path.toFile());
            outputStream.write(res.bodyAsBytes());
            outputStream.close();
        } catch (IOException e) {
            // System.out.println(String.format("Error: %s", file.getPath()));
            // throw new RuntimeException(e);
            e.printStackTrace();
            // System.exit(1);
            return;
        }
    }
}
