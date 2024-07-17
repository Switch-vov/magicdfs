package com.switchvov.magicdfs.syncer;

import java.io.File;

/**
 * @author switch
 * @since 2024/07/15
 */
public interface FileSyncer {
    String X_FILENAME = "X-Filename";
    boolean sync(File file, String backupUrl, boolean sync);
}
