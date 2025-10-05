package rs.jamie.luneth;

import org.jetbrains.annotations.NotNull;
import rs.jamie.luneth.modules.*;
import rs.jamie.luneth.modules.Module;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class LunethManager {

    private final Module module;

    private LunethManager(Module module) {
        this.module = module;
    }

    @SuppressWarnings("unchecked")
    private <K> ByteBuffer getKey(StorageObject object) {
        Field field = LunethReflection.getKeyField(object);
        try {
            StorageSerializer<K> serializer = (StorageSerializer<K>) LunethSerializers.getSerializer(field.getType());
            return ByteBuffer.wrap(serializer.set(LunethReflection.getValueObject(field, object)));
        } catch (Exception e) {
            throw new RuntimeException("[Luneth] Error executing Luneth:getKey()/1", e);
        }
    }

    @SuppressWarnings("unchecked")
    private <K> ByteBuffer getKey(Class<? extends StorageObject> clazz, K key) {
        Field field = LunethReflection.getKeyField(clazz);
        try {
            StorageSerializer<K> serializer = (StorageSerializer<K>) LunethSerializers.getSerializer(field.getType());
            return ByteBuffer.wrap(serializer.set(key));
        } catch (Exception e) {
            throw new RuntimeException("[Luneth] Error executing Luneth:getKey()/2", e);
        }
    }

    @SuppressWarnings("unchecked")
    private <K> List<ByteBuffer> getValues(StorageObject object) {
        List<ByteBuffer> values = new ArrayList<>();
        for (Field field : LunethReflection.getValueFields(object)) {
            try {
                StorageSerializer<K> serializer = (StorageSerializer<K>) LunethSerializers.getSerializer(field.getType());
                values.add(ByteBuffer.wrap(serializer.set(LunethReflection.getValueObject(field, object))));
            } catch (Exception e) {
                throw new RuntimeException("[Luneth] Error executing SQL:getValues()", e);
            }
        }

        return values;
    }

    public CompletableFuture<Boolean> put(StorageObject object) {
        String id = LunethReflection.getIdentifier(object);
        if(id == null) throw new IllegalArgumentException("StorageObject is not registered correctly "+object.getClass().getTypeName());

        List<ByteBuffer> values = getValues(object);
        int totalsize = 0;
        int valuetotal = 0;
        for (ByteBuffer value : values) {
            totalsize += value.capacity();
            valuetotal++;
        }

        ByteBuffer buffer = ByteBuffer.allocate((Integer.BYTES) + totalsize + (Integer.BYTES * valuetotal));
        buffer.putInt(valuetotal);
        for (ByteBuffer value : values) {
            int size = value.capacity();
            buffer.putInt(size);
            byte[] bytes = new byte[size];
            value.get(bytes, 0, value.remaining());
            buffer.put(bytes);
        }

        buffer.flip();
        CompletableFuture<Boolean> future = module.setObject(getKey(object), buffer, id);
        future.exceptionally((e) -> {
            throw new RuntimeException("[Luneth] Error executing SQL:put()", e);
        });

        return future;
    }

    public <K, T extends StorageObject> CompletableFuture<T> get(Class<T> clazz, K key) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String id = LunethReflection.getIdentifier(clazz);
                if(id == null) throw new IllegalArgumentException("StorageObject is not registered correctly "+clazz.getName());
                CompletableFuture<ByteBuffer> future = module.getObject(getKey(clazz, key), id);
                future.exceptionally((e) -> {
                    throw new RuntimeException("[Luneth] Error executing SQL:get()", e);
                });
                ByteBuffer buffer = future.join();
                if(buffer==null) return null;

                List<ByteBuffer> values = new ArrayList<>();
                int valueTotal = buffer.getInt();
                for (int i=0; i < valueTotal; i++) {
                    int size = buffer.getInt();
                    byte[] bytes = new byte[size];
                    buffer.get(bytes, 0, size);
                    values.add(ByteBuffer.wrap(bytes));
                }

                List<Field> fields = LunethReflection.getValueFields(clazz);
                List<Object> objects = new ArrayList<>();
                objects.add(key);

                for(int i=0; i < fields.size(); i++) {
                    Field field = fields.get(i);
                    ByteBuffer value = values.get(i);
                    StorageSerializer<K> serializer = (StorageSerializer<K>) LunethSerializers.getSerializer(field.getType());
                    if(serializer == null) {
                        throw new RuntimeException("[Luneth] Error executing Luneth:get() | Null Serializer For: " + field.getType());
                    }
                    objects.add(serializer.get(value));
                }

                Constructor<?>[] constructors = clazz.getConstructors();
                for (Constructor<?> constructor : constructors) {
                    Class<?>[] paramTypes = constructor.getParameterTypes();
                    if (paramTypes.length != objects.size()) continue;
                    boolean match = true;
                    for (int i = 0; i < paramTypes.length; i++) {
                        if (!paramTypes[i].isAssignableFrom(objects.get(i).getClass())) {
                            match = false;
                            break;
                        }
                    }

                    if (match) {
                        //noinspection unchecked
                        return (T) constructor.newInstance(objects.toArray());
                    }
                }

                throw new IllegalStateException("No suitable constructor found for " + clazz.getName());
            } catch (Exception e) {
                throw new RuntimeException("[Luneth] Error executing SQL:get()", e);
            }
        });
    }

    public <K, T extends StorageObject> CompletableFuture<Boolean> remove(Class<T> clazz, K key) {
        return CompletableFuture.supplyAsync(() -> {
            String id = LunethReflection.getIdentifier(clazz);
            if(id == null) throw new IllegalArgumentException("StorageObject is not registered correctly "+clazz.getTypeName());
            CompletableFuture<Boolean> future = module.removeObject(getKey(clazz, key), id);
            future.exceptionally((e) -> {
                throw new RuntimeException("[Luneth] Error executing Luneth:remove()", e);
            });

            return future.join();
        });
    }

    public <K> void registerSerializer(Class<?> clazz, StorageSerializer<K> serializer) {
        LunethSerializers.register(clazz, serializer, true);
    }

    public StorageSerializer<?> getSerializer(Class<?> clazz) {
        return LunethSerializers.getSerializer(clazz);
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
        private Duration cacheDuration = Duration.ZERO;
        private List<String> packages = new ArrayList<>();

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
        public @NotNull Builder setExpiration(Duration duration) {
            this.cacheDuration = duration;
            return this;
        }

        /**
         * Register a package containing serializers
         * @param pkg your main classpath
         */
        public @NotNull Builder registerPackage(String pkg) {
            this.packages.add(pkg);
            return this;
        }

        public @NotNull LunethManager build() {
            Module module;
            switch (storageMode) {
                case REDIS -> {
                    module = new RedisModule(connectionUrl, cacheDuration);
                }
                case SQL -> {
                    module = new SQLModule(connectionUrl);
                }
                case MONGODB -> {
                    module = new MongoModule(connectionUrl, cacheDuration);
                }
                default -> {
                    module = new CaffeineModule(cacheDuration);
                }
            }

            LunethSerializers.registerPackage("rs.jamie.luneth.objects", true);

            for(String pkg : packages) {
                LunethSerializers.registerPackage(pkg, false);
            }

            System.out.println("nag");
            return new LunethManager(module);
        }

    }


}
