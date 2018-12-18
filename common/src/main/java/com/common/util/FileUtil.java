package com.common.util;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {

//    public static String find(String[] paths, String file) throws FileNotFoundException {
//        if (file == null) throw new FileNotFoundException("the filename is null");
//        if (file.trim().isEmpty()) throw new FileNotFoundException("the filename is empty");
//
//        for (String path : paths) {
//            if (Files.exists(Paths.get(path + file)))
//                return path + file;
//        }
//
//        throw new FileNotFoundException(file);
//    }

    public static Path find(Path file, Path[] paths) throws FileNotFoundException {
        Path find;
        for (Path path : paths) {
            find = path.resolve(file);
            if (Files.exists(find))
                return find;
        }
        throw new FileNotFoundException(file.toString());
    }

//    public static String sha256Hex(String file) throws IOException {
//        try (InputStream is = Files.newInputStream(Paths.get(file))) {
//            return DigestUtils.sha256Hex(is);
//        }
//    }

    public static String sha256Hex(Path file) throws IOException {
        try (InputStream stream = Files.newInputStream(file)) {
            return DigestUtils.sha256Hex(stream);
        }
    }

//    public static boolean exists(String[] paths, String file) throws FileNotFoundException {
//        if (file == null) throw new FileNotFoundException("the filename is null");
//        if (file.trim().isEmpty()) throw new FileNotFoundException("the filename is empty");
//
//        for (String path : paths) {
//            if (Files.exists(Paths.get(path + file)))
//                return true;
//        }
//        return false;
//    }

    public static boolean exists(Path[] paths, Path file) throws FileNotFoundException {
        for (Path path : paths) {
            if (Files.exists(path.resolve(file)))
                return true;
        }
        return false;
    }

//    public static boolean notExists(String[] paths, String file) throws FileNotFoundException {
//        if (file == null) throw new FileNotFoundException("the filename is null");
//        if (file.trim().isEmpty()) throw new FileNotFoundException("the filename is empty");
//
//        for (String path : paths) {
//            if (Files.notExists(Paths.get(path + file)))
//                return true;
//        }
//        return false;
//    }

    public static boolean notExists(Path[] paths, Path file) throws FileNotFoundException {
        for (Path path : paths) {
            if (Files.notExists(path.resolve(file)))
                return true;
        }
        return false;
    }

}
