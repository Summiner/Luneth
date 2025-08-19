package rs.jamie.luneth.modules;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class MongoModule implements Module {

    private static final Charset charSet = StandardCharsets.UTF_8;
    private final Duration cacheDuration;

    public MongoModule(String url, Duration cacheDuration) {
        this.cacheDuration = cacheDuration;
    }

    @Override
    public CompletableFuture<ByteBuffer> getObject(ByteBuffer key, String identifier) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> setObject(ByteBuffer key, ByteBuffer value, String identifier) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> removeObject(ByteBuffer key, String identifier) {
        return null;
    }

    @Override
    public void createTable(String name) {
        //
    }

}
