package com.gempukku.lotro.async;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * The static files already read on one connection. A file that has changed on disk since is read again, so a deployment
 * reaches connections that were open while it happened: the Angular app's page names scripts by the hash of their
 * contents, so an old copy of the page would ask for scripts that may no longer exist.
 */
class ConnectionFileCache {
    private record CachedFile(long lastModified, long length, byte[] bytes) {
        boolean isCurrent(long lastModifiedNow, long lengthNow) {
            return lastModified == lastModifiedNow && length == lengthNow;
        }
    }

    private final Map<String, CachedFile> _files = new HashMap<>();

    /** The file's contents; the caller has checked that it is a file. */
    synchronized byte[] read(File file) throws IOException {
        String path = file.getCanonicalPath();
        long lastModified = file.lastModified();
        long length = file.length();
        CachedFile cached = _files.get(path);
        if (cached != null && cached.isCurrent(lastModified, length)) {
            return cached.bytes();
        }
        byte[] bytes = Files.readAllBytes(file.toPath());
        _files.put(path, new CachedFile(lastModified, length, bytes));
        return bytes;
    }
}
