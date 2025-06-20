package rs.jamie.luneth.modules;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class CaffeineModule implements Module {

    private final AsyncCache<ByteBuffer, ByteBuffer> cache;
    private static final Charset charSet = StandardCharsets.UTF_8;

    public CaffeineModule(Integer cacheTime) {
        if(cacheTime==0) {
            cache = Caffeine.newBuilder().buildAsync();
        } else {
            cache = Caffeine.newBuilder()
                    .expireAfterWrite(cacheTime, TimeUnit.SECONDS)
                    .buildAsync();
        }
    }

    @Override
    public CompletableFuture<ByteBuffer> getObject(ByteBuffer key, String identifier) {
        if (!identifier.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name: " + identifier);
        }
        CompletableFuture<ByteBuffer> buffer = cache.getIfPresent(addIdentifier(key, identifier));
        return buffer!=null?buffer:CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Boolean> setObject(ByteBuffer key, ByteBuffer value, String identifier) {
        if (!identifier.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name: " + identifier);
        }
        try {
            cache.put(addIdentifier(key, identifier), CompletableFuture.completedFuture(value));
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            return CompletableFuture.completedFuture(false);
        }
    }

    @Override
    public CompletableFuture<Boolean> removeObject(ByteBuffer key, String identifier) {
        if (!identifier.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name: " + identifier);
        }
        try {
            cache.synchronous().invalidate(addIdentifier(key, identifier));
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            return CompletableFuture.completedFuture(false);
        }
    }

    private ByteBuffer addIdentifier(ByteBuffer buffer, String identifier) {
        if(identifier==null) return null;
        byte[] id = identifier.getBytes(charSet);
        int size = id.length;
        ByteBuffer buf = ByteBuffer.allocate(buffer.remaining() + Integer.BYTES + size);
        buf.putInt(size);
        buf.put(id);
        buf.put(buffer);
        return buf.flip();
    }
}
