package com.server.util;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtil {

    public static String find(String[] paths, String file) throws FileNotFoundException {
        if ("".equals(file)) throw new FileNotFoundException("the id is empty");

        int len = paths.length;
        for (int i = 0; i < len; i++) {
            if (Files.exists(Paths.get(paths[i] + file)))
                return paths[i] + file;
        }

        throw new FileNotFoundException(file);
    }

}
