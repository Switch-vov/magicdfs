package com.switchvov.magicdfs.syncer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;

/**
 * sync file to backup server.
 *
 * @author switch
 * @since 2024/07/15
 */
@Component
@Slf4j
public class HttpSyncer {
    public static final String X_FILENAME = "X-Filename";
    public static final String X_ORIG_FILENAME = "X-Orig-Filename";

    public String sync(File file, String backupUrl, String originalFilename) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set(X_FILENAME, file.getName());
        headers.set(X_ORIG_FILENAME, originalFilename);

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new FileSystemResource(file));

        HttpEntity<MultiValueMap<String, HttpEntity<?>>> httpEntity = new HttpEntity<>(builder.build(), headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(backupUrl, httpEntity, String.class);
        String result = responseEntity.getBody();
        log.info("  ===>[MagicDFS] sync result: {}", result);
        return result;
    }
}
