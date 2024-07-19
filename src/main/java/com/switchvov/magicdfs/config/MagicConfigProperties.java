package com.switchvov.magicdfs.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * config properties.
 *
 * @author switch
 * @since 2024/7/19
 */
@ConfigurationProperties(prefix = "mdfs")
@Data
public class MagicConfigProperties {
    private String uploadPath;
    private String backupUrl;
    private String downloadUrl;
    private String group;
    private boolean autoMd5;
    private boolean syncBackup;
    private String backupTopic;
}
