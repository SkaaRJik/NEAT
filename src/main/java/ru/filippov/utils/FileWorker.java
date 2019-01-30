package ru.filippov.utils;

import java.io.*;

public class FileWorker {
    public static boolean makeCopy(String filePath, String destination) throws IOException {
        File file = new File(filePath);
        if(!file.exists()) return false;
        File dest = new File(destination);
        InputStream is = new FileInputStream(file);
        OutputStream os = new FileOutputStream(dest);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) > 0) {
            os.write(buffer, 0, length);
        }
        is.close();
        os.close();
        return true;
    }
}