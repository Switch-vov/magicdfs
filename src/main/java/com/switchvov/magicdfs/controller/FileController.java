package com.switchvov.magicdfs.controller;

import com.switchvov.magicdfs.config.MagicConfigProperties;
import com.switchvov.magicdfs.model.FileMeta;
import com.switchvov.magicdfs.syncer.HttpSyncer;
import com.switchvov.magicdfs.syncer.MQSyncer;
import com.switchvov.magicdfs.util.FileUtil;
import com.switchvov.magicutils.JsonUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.util.Objects;

/**
 * @author switch
 * @since 2024/07/15
 */
@Slf4j
@RestController
public class FileController {
    private final MagicConfigProperties configProperties;
    private final HttpSyncer httpSyncer;
    private final MQSyncer mqSyncer;

    public FileController(
            @Autowired MagicConfigProperties configProperties,
            @Autowired HttpSyncer httpSyncer,
            @Autowired MQSyncer mqSyncer
    ) {
        this.configProperties = configProperties;
        this.httpSyncer = httpSyncer;
        this.mqSyncer = mqSyncer;
    }

    @SneakyThrows
    @PostMapping("/upload")
    public String upload(@RequestParam MultipartFile file, HttpServletRequest request) {

        // 1. 处理文件
        boolean needSync = false;
        String fileName = request.getHeader(HttpSyncer.X_FILENAME);
        String originalFilename = file.getOriginalFilename();

        if (Objects.isNull(fileName) || fileName.isEmpty()) {
            // 正常上传
            needSync = true;
            fileName = FileUtil.getUUIDFile(originalFilename);
        } else {
            // 主从同步文件
            String xor = request.getHeader(HttpSyncer.X_ORIG_FILENAME);
            if (Objects.nonNull(xor) && !xor.isEmpty()) {
                originalFilename = xor;
            }
        }

        String subDir = FileUtil.getSubDir(fileName);
        File dest = getFile(subDir, fileName);

        // 复制文件到指定位置
        file.transferTo(dest);
        log.info(" ===>[MagicDFS] dir: {} file name: {} size: {}", subDir, originalFilename, file.getSize());

        // 2. 处理meta
        FileMeta meta = new FileMeta(fileName, originalFilename, file.getSize(), configProperties.getDownloadUrl());
        if (configProperties.isAutoMd5()) {
            meta.getTags().put("md5", DigestUtils.md5DigestAsHex(new FileInputStream(dest)));
        }
        // 2.1 存放到本地文件
        FileUtil.writeMeta(new File(dest.getAbsoluteFile() + ".meta"), meta);

        // TODO:code 存放到数据库
        // TODO:code 存放到配置中心或注册中心

        // 3. 同步到backup
        if (needSync) {
            if (configProperties.isSyncBackup()) {
                try {
                    httpSyncer.sync(dest, configProperties.getBackupUrl(), originalFilename);
                } catch (Exception e) {
                    e.printStackTrace();
                    // 同步失败则转异步处理
                    mqSyncer.sync(meta);
                }
            } else {
                mqSyncer.sync(meta);
            }
        }
        return fileName;
    }


    private File getFile(String subDir, String filename) {
        return new File(configProperties.getUploadPath() + "/" + subDir + "/" + filename);
    }

    @SneakyThrows
    @RequestMapping("/download")
    public void download(String name, HttpServletResponse response) {
        File file = getFile(FileUtil.getSubDir(name), name);
        log.info(" ===>[MagicDFS] file path: {}", file.getAbsolutePath());

        response.setCharacterEncoding("UTF-8");
        response.setContentType(FileUtil.getMineType(name));
        response.addHeader("Content-Length", "" + file.length());
        FileUtil.output(file, response.getOutputStream());
    }

    @SneakyThrows
    @RequestMapping("/meta")
    public FileMeta meta(String name) {
        String metaJson = FileUtil.readString(getFile(FileUtil.getSubDir(name), name + ".meta"));
        return JsonUtils.fromJson(metaJson, FileMeta.class);
    }
}
