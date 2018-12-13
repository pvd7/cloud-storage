package com.common.util;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileUtil {

    public static String exists(String[] paths, String file) throws FileNotFoundException {
        if ("".equals(file)) throw new FileNotFoundException("the uuid is empty");

        int len = paths.length;
        for (int i = 0; i < len; i++) {
            if (Files.exists(Paths.get(paths[i] + file)))
                return paths[i] + file;
        }

        throw new FileNotFoundException("the uuid is empty");
    }

    public static String sha256Hex(String file) throws IOException {
        try (InputStream is = Files.newInputStream(Paths.get(file))) {
            return DigestUtils.sha256Hex(is);
        }
    }
}
