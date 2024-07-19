package com.switchvov.magicdfs;

import com.switchvov.magicdfs.config.MagicConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import static com.switchvov.magicdfs.util.FileUtil.init;

@SpringBootApplication
@Import(RocketMQAutoConfiguration.class)
@EnableConfigurationProperties(MagicConfigProperties.class)
@Slf4j
public class MagicDFSApplication {

    public static void main(String[] args) {
        SpringApplication.run(MagicDFSApplication.class, args);
    }

    @Value("${mdfs.uploadPath}")
    private String uploadPath;

    @Bean
    public ApplicationRunner runner() {
        return args -> {
            init(uploadPath);
            log.info(" ===>[MagicDFS] started");
        };
    }

}
