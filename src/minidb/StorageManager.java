package minidb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class StorageManager {

    private final String filePath;

    public StorageManager(String filePath) {
        this.filePath = filePath;
    }

    public void save(Database database) throws IOException {
        File file = new File(filePath);
        File tempFile = new File(filePath + ".tmp");

        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(tempFile))) {
            out.writeObject(database);
        }

        Files.move(
                tempFile.toPath(),
                file.toPath(),
                StandardCopyOption.REPLACE_EXISTING
        );
    }

    public Database load() throws IOException, ClassNotFoundException {
        File file = new File(filePath);

        if (!file.exists()) {
            return new Database();
        }

        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(file))) {
            return (Database) in.readObject();
        }
    }
}