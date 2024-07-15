package com.switchvov.magicdfs.syncer;

import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Objects;

/**
 * @author switch
 * @since 2024/07/15
 */
@Component
public class FileSyncerImpl implements FileSyncer {
    private HttpSyncer httpSyncer = new HttpSyncer();

    @Override
    public boolean sync(File file, String backupUrl, boolean sync) {
        if (Objects.isNull(backupUrl) || "null".equals(backupUrl)) {
            return false;
        }

        if (sync) {
            httpSyncer.sync(file, backupUrl, sync);
        }
        return true;
    }
}
