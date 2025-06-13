package rs.jamie.luneth;

import org.jetbrains.annotations.NotNull;
import rs.jamie.luneth.modules.CaffeineModule;
import rs.jamie.luneth.modules.Module;
import rs.jamie.luneth.modules.RedisModule;
import rs.jamie.luneth.modules.SQLModule;

import java.util.concurrent.CompletableFuture;

public class LunethManager {

    private final Module module;

    private LunethManager(Module module) {
        this.module = module;
    }

    public CompletableFuture<Object> getObject(StorageObject<?, ?> object) {
        return CompletableFuture.supplyAsync(() -> object.decodeValue(module.getObject(object.encodeKey(), object.getIdentifier()).join()));
    }

    public CompletableFuture<Boolean> setObject(StorageObject<?, ?> object) {
        return module.setObject(object.encodeKey(), object.encodeValue(), object.getIdentifier());
    }

    public enum StorageModes {
        // Temporary Types
        REDIS,
        CAFFEINE,

        // Persistent Types
        SQL,
        MONGODB,
        SCYLLA
    }


    public static class Builder {

        private StorageModes storageMode = StorageModes.CAFFEINE;
        private String connectionUrl = "redis://localhost";
        private int cacheTime = 0;

        /**
         * Set the storage mode for objects.
         * Defaults to Caffeine
         * @param storageMode storage mode used by Luneth
         * @return this builder
         */
        public @NotNull Builder setStorageMode(@NotNull StorageModes storageMode) {
            this.storageMode = storageMode;
            return this;
        }

        /**
         * Set the redis server url.
         * Defaults to redis://localhost
         * @param connectionUrl url used to connect to redis server
         * @return this builder
         */
        public @NotNull Builder setConnectionURL(@NotNull String connectionUrl) {
            this.connectionUrl = connectionUrl;
            return this;
        }

        /**
         * Set the cache duration for temporary storage modes.
         * Defaults to 0 (0 = disabled)
         * @param duration duration in seconds to cache the object
         * @return this builder
         */
        public @NotNull Builder setCacheDuration(int duration) {
            this.cacheTime = duration;
            return this;
        }

        public @NotNull LunethManager build() {
            Module module;
            switch (storageMode) {
                case REDIS -> {
                    module = new RedisModule(connectionUrl, cacheTime);
                }
                case SQL -> {
                    module = new SQLModule(connectionUrl);
                }
                default -> {
                    module = new CaffeineModule(cacheTime);
                }
            }
            return new LunethManager(module);
        }

    }


}
