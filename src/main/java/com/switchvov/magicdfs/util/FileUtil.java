package com.switchvov.magicdfs.util;

import com.switchvov.magicdfs.model.FileMeta;
import com.switchvov.magicutils.JsonUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.UUID;

/**
 * @author switch
 * @since 2024/07/17
 */
@Slf4j
public class FileUtil {
    private final static String DEFAULT_MINE_TYPE = "application/octst-stream";

    public static String getMineType(String fileName) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String content = fileNameMap.getContentTypeFor(fileName);
        return Objects.isNull(content) ? DEFAULT_MINE_TYPE : content;
    }

    public static void init(String uploadPath) {
        log.info(" ===>[MagicDFS] init kdfs dirs...");
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // 输出256个文件夹，名称为十六进制
        for (int i = 0; i < 256; i++) {
            String subDir = String.format("%02x", i);
            File dirPath = new File(uploadPath, subDir);
            if (!dirPath.exists()) {
                dirPath.mkdirs();
            }
        }
    }

    public static String getUUIDFile(String file) {
        return UUID.randomUUID().toString() + getExt(file);
    }

    public static String getSubDir(String file) {
        return file.substring(0, 2);
    }

    private static String getExt(String originalFileName) {
        return originalFileName.substring(originalFileName.lastIndexOf("."));
    }

    @SneakyThrows
    public static void writeMeta(File metaFile, FileMeta meta) {
        String metaJson = JsonUtils.toJson(meta);
        Files.writeString(Paths.get(metaFile.getAbsolutePath()), metaJson,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }

}
