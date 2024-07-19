package com.switchvov.magicdfs.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * file meta data.
 *
 * @author switch
 * @since 2024/07/17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileMeta {
    private String name;
    private String originalFilename;
    private long size;
    private String downloadUrl;
    private Map<String, String> tags = new HashMap<>();

    public FileMeta(String name, String originalFilename, long size, String downloadUrl) {
        this.name = name;
        this.originalFilename = originalFilename;
        this.size = size;
        this.downloadUrl = downloadUrl;
    }
}
