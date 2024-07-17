package com.switchvov.magicdfs.controller;

import com.switchvov.magicdfs.model.FileMeta;
import com.switchvov.magicdfs.syncer.FileSyncer;
import com.switchvov.magicdfs.util.FileUtil;
import com.switchvov.magicutils.JsonUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.DigestUtils;
import org.springframework.util.FileCopyUtils;
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
    @Value("${mdfs.autoMd5}")
    private boolean autoMd5;

    @Autowired
    private FileSyncer syncer;

    @SneakyThrows
    @PostMapping("/upload")
    public String upload(@RequestParam MultipartFile file, HttpServletRequest request) {

        // 1. 处理文件
        boolean needSync = false;
        String fileName = request.getHeader(FileSyncer.X_FILENAME);
        if (Objects.isNull(fileName) || fileName.isEmpty()) {
            fileName = FileUtil.getUUIDFile(file.getOriginalFilename());
            needSync = true;
        }
        String subDir = FileUtil.getSubDir(fileName);
        File dest = new File(uploadPath + "/" + subDir + "/" + fileName);
        file.transferTo(dest);
        log.info(" ===>[MagicDFS] dir: {} file name: {} size: {}", subDir, file.getOriginalFilename(), file.getSize());

        // 2. 处理meta
        FileMeta meta = new FileMeta();
        meta.setName(fileName);
        meta.setOriginalFilename(file.getOriginalFilename());
        meta.setSize(file.getSize());
        if (autoMd5) {
            meta.getTags().put("md5", DigestUtils.md5DigestAsHex(new FileInputStream(dest)));
        }
        // 2.1 存放到本地文件
        String metaName = fileName + ".meta";
        File metaFile = new File(uploadPath + "/" + subDir + "/" + metaName);
        FileUtil.writeMeta(metaFile, meta);

        // TODO:code 存放到数据库
        // TODO:code 存放到配置中心或注册中心

        // 3. 同步到backup
        if (needSync) {
            syncer.sync(dest, backupUrl, isSync);
        }
        return fileName;
    }

    @SneakyThrows
    @RequestMapping("/download")
    public void download(String name, HttpServletResponse response) {
        String subDir = FileUtil.getSubDir(name);
        String path = uploadPath + "/" + subDir + "/" + name;
        File file = new File(path);
        log.info(" ===>[MagicDFS] file path: {}", file.getAbsolutePath());

        // 将文件写入输入流
        FileInputStream fileInputStream = new FileInputStream(file);
        InputStream is = new BufferedInputStream(fileInputStream);
        byte[] buffer = new byte[16 * 1024];

        // 加一些response的头
        response.setCharacterEncoding("UTF-8");
        response.setContentType(FileUtil.getMineType(name));
        response.addHeader("Content-Length", "" + file.length());

        // 读取文件信息，并逐段输出
        OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
        while (is.read(buffer) > 0) {
            outputStream.write(buffer);
        }
        outputStream.flush();
        is.close();
    }

    @SneakyThrows
    @RequestMapping("/meta")
    public FileMeta meta(String name) {
        String subDir = FileUtil.getSubDir(name);
        String path = uploadPath + "/" + subDir + "/" + name + ".meta";
        File file = new File(path);
        String metaJson = FileCopyUtils.copyToString(new FileReader(file));
        return JsonUtils.fromJson(metaJson, FileMeta.class);
    }
}
