package de.agrirouter.middleware.business.cache.messaging;

import lombok.extern.slf4j.Slf4j;
import one.microstream.reflect.ClassLoaderProvider;
import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * Provides the storage manager.
 */
@Slf4j
@Component
public class StorageManagerProvider {

    @Value("${app.cache.message-cache.data-directory}")
    private String dataDirectory;

    @Bean
    protected EmbeddedStorageManager embeddedStorageManager() {
        CacheRoot cacheRoot = new CacheRoot();
        log.info("Using data directory: {}", dataDirectory);
        return EmbeddedStorage.Foundation(Paths.get(dataDirectory))
                .onConnectionFoundation(cf -> cf.setClassLoaderProvider(ClassLoaderProvider.New(
                        Thread.currentThread().getContextClassLoader()))).start(cacheRoot);
    }

}
