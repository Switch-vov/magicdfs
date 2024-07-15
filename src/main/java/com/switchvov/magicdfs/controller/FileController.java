package com.switchvov.magicdfs.controller;

import com.switchvov.magicdfs.syncer.FileSyncer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

/**
 * @author switch
 * @since 2024/07/15
 */
@Slf4j
@RestController
public class FileController {
    @Value("${mdfs.path}")
    private String uploadPath;
    @Value("${mdfs.isSync}")
    private boolean isSync;
    @Value("${mdfs.backupUrl}")
    private String backupUrl;

    @Autowired
    private FileSyncer syncer;

    @SneakyThrows
    @PostMapping("/upload")
    public String upload(@RequestParam MultipartFile file, HttpServletRequest request) {
        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = request.getHeader("X-Filename");
        boolean sync = false;
        if (Objects.isNull(fileName) || fileName.isEmpty()) {
            fileName = UUID.randomUUID().toString() + ext;
            sync = true;
        }
        String dir = fileName.substring(0, 2);
        log.info(" ===>[MagicDFS] dir: {} file name: {} size: {}", dir, file.getOriginalFilename(), file.getSize());
        File dest = new File(uploadPath + "/" + dir + "/" + fileName);
        file.transferTo(dest);
        if (sync) {
            syncer.sync(dest, backupUrl, isSync);
        }
        return fileName;
    }

    @SneakyThrows
    @RequestMapping("/download")
    public void download(String name, HttpServletResponse response) {
        String dir = name.substring(0, 2);
        String path = uploadPath + "/" + dir + "/" + name;
        File file = new File(path);
        log.info(" ===>[MagicDFS] file path: {}", file.getPath());
        String filename = file.getName();
        // 将文件写入输入流
        FileInputStream fileInputStream = new FileInputStream(file);
        InputStream fis = new BufferedInputStream(fileInputStream);
        byte[] buffer = new byte[16 * 1024];
        ;
        response.setCharacterEncoding("UTF-8");
        response.addHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
        response.addHeader("Content-Length", "" + file.length());
        OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
        response.setContentType("application/octet-stream");

        while (fis.read(buffer) > 0) {
            outputStream.write(buffer);
        }
        fis.close();
        outputStream.flush();
    }
}
