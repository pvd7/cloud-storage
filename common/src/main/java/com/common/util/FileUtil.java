package com.common.util;

import org.apache.commons.codec.digest.DigestUtils;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtil {

    public static String find(String[] paths, String file) throws FileNotFoundException {
        if (file == null) throw new FileNotFoundException("the filename is null");
        if (file.trim().isEmpty()) throw new FileNotFoundException("the filename is empty");

        for (String path : paths) {
            if (Files.exists(Paths.get(path + file)))
                return path + file;
        }

        throw new FileNotFoundException(file);
    }

    public static String sha256Hex(String file) throws IOException {
        try (InputStream is = Files.newInputStream(Paths.get(file))) {
            return DigestUtils.sha256Hex(is);
        }
    }

    public static boolean exists(String[] paths, String file) throws FileNotFoundException {
        if (file == null) throw new FileNotFoundException("the filename is null");
        if (file.trim().isEmpty()) throw new FileNotFoundException("the filename is empty");

        for (String path : paths) {
            if (Files.exists(Paths.get(path + file)))
                return true;
        }

        return false;
    }

}
