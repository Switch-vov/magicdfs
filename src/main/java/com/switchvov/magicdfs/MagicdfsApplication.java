package com.switchvov.magicdfs;

import jakarta.servlet.MultipartConfigElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;

import java.io.File;

@SpringBootApplication
@Slf4j
public class MagicdfsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MagicdfsApplication.class, args);
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setLocation("/private/tmp/tomcat");
        return factory.createMultipartConfig();
    }

    @Value("${mdfs.path}")
    private String uploadPath;

    @Bean
    public ApplicationRunner runner() {
        return args -> {
            log.info(" ===>[MagicDFS] init kdfs dirs...");
            File path = new File(uploadPath);
            if (!path.exists()) {
                path.mkdirs();
            }
            // 输出256个文件夹，名称为十六进制
            for (int i = 0; i < 256; i++) {
                String dir = String.format("%02x", i);
                File dirPath = new File(uploadPath, dir);
                if (!dirPath.exists()) {
                    dirPath.mkdirs();
                }
                File tmpDirPath = new File("/private/tmp/tomcat/" + uploadPath, dir);
                if (!tmpDirPath.exists()) {
                    tmpDirPath.mkdirs();
                }
            }
        };
    }

}
