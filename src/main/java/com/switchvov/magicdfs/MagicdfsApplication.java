package com.switchvov.magicdfs;

import com.switchvov.magicdfs.util.FileUtil;
import jakarta.servlet.MultipartConfigElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;

import java.io.File;

import static com.switchvov.magicdfs.util.FileUtil.init;

@SpringBootApplication
@Slf4j
public class MagicdfsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MagicdfsApplication.class, args);
    }

    @Value("${mdfs.path}")
    private String uploadPath;

    @Bean
    public ApplicationRunner runner() {
        return args -> {
            init(uploadPath);
            log.info(" ===>[MagicDFS] started");
        };
    }

}
