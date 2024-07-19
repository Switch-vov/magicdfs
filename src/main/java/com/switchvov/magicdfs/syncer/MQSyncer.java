package com.switchvov.magicdfs.syncer;

import com.switchvov.magicdfs.config.MagicConfigProperties;
import com.switchvov.magicdfs.model.FileMeta;
import com.switchvov.magicdfs.util.FileUtil;
import com.switchvov.magicutils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Objects;

/**
 * mq syncer.
 *
 * @author switch
 * @since 2024/7/19
 */
@Component
@Slf4j
public class MQSyncer {
    private final MagicConfigProperties configProperties;
    private final RocketMQTemplate rocketMQTemplate;

    public MQSyncer(
            @Autowired MagicConfigProperties magicConfigProperties,
            @Autowired RocketMQTemplate rocketMQTemplate
    ) {
        this.configProperties = magicConfigProperties;
        this.rocketMQTemplate = rocketMQTemplate;
    }

    public void sync(FileMeta meta) {
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJson(meta)).build();
        rocketMQTemplate.send(configProperties.getBackupTopic(), message);
        log.debug(" ===>[MagicDFS] send message: {}", message);
    }

    @Service
    @RocketMQMessageListener(topic = "${mdfs.backupTopic}", consumerGroup = "${mdfs.group}")
    public class FileMQSyncer implements RocketMQListener<MessageExt> {

        @Override
        public void onMessage(MessageExt message) {
            // 1. 从消息里拿到meta数据
            log.debug(" ===>[MagicDFS] onMessage ID: {}", message.getMsgId());
            String msgJson = new String(message.getBody());
            log.debug(" ===>[MagicDFS] onMessage JSON: {}", msgJson);
            FileMeta meta = JsonUtils.fromJson(msgJson, FileMeta.class);
            String downloadUrl = meta.getDownloadUrl();
            if (Objects.isNull(downloadUrl) || downloadUrl.isEmpty()) {
                log.debug(" ===>[MagicDFS] onMessage downloadUrl is empty.");
                return;
            }

            // 去重本机操作
            if (configProperties.getDownloadUrl().equals(downloadUrl)) {
                log.debug(" ===>[MagicDFS] onMessage the same file server, ignore mq sync task.");
                return;
            }
            log.debug(" ===>[MagicDFS] onMessage the other file server, process mq sync task.");

            // 2. 写meta文件
            String dir = configProperties.getUploadPath() + "/" + FileUtil.getSubDir(meta.getName());
            File metaFile = new File(dir, meta.getName() + ".meta");
            if (metaFile.exists()) {
                log.debug(" ===>[MagicDFS] onMessage meta file exists and ignore save: {}", metaFile.getAbsoluteFile());
            } else {
                log.debug(" ===>[MagicDFS] onMessage meta file save: {}", metaFile.getAbsoluteFile());
                FileUtil.writeString(metaFile, msgJson);
            }

            // 3. 下载文件
            File file = new File(dir, meta.getName());
            if (file.exists() && file.length() == meta.getSize()) {
                log.debug(" ===>[MagicDFS] onMessage file exists and ignore download: {}", file.getAbsoluteFile());
                return;
            }
            String download = downloadUrl + "?name=" + file.getName();
            FileUtil.download(download, file);
        }
    }
}
