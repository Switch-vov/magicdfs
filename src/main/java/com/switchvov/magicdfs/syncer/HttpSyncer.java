package com.switchvov.magicdfs.syncer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;

/**
 * @author switch
 * @since 2024/07/15
 */
@Slf4j
public class HttpSyncer implements FileSyncer {
    @Override
    public boolean sync(File file, String backupUrl, boolean sync) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set(X_FILENAME, file.getName());
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new FileSystemResource(file));

        HttpEntity<MultiValueMap<String, HttpEntity<?>>> httpEntity = new HttpEntity<>(builder.build(), headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(backupUrl, httpEntity, String.class);
        log.info("  ===>[MagicDFS] sync result: {}", responseEntity.getBody());
        return true;
    }
}
