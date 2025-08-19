package rs.jamie.luneth.modules;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class CaffeineModule implements Module {

    private final AsyncCache<String, ByteBuffer> cache;
    private static final Charset charSet = StandardCharsets.UTF_8;

    public CaffeineModule(Duration cacheDuration) {
        if(cacheDuration.getNano()==0) {
            cache = Caffeine.newBuilder().buildAsync();
        } else {
            cache = Caffeine.newBuilder()
                    .expireAfterWrite(cacheDuration.getNano(), TimeUnit.NANOSECONDS)
                    .buildAsync();
        }
    }

    @Override
    public CompletableFuture<@Nullable ByteBuffer> getObject(ByteBuffer key, String identifier) {
        if(key == null) return CompletableFuture.completedFuture(null);
        if (!identifier.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name: " + identifier);
        }
        String str = cachekey(key, identifier);
        CompletableFuture<ByteBuffer> buffer = cache.getIfPresent(str);
        return buffer!=null?buffer:CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<@Nullable Boolean> setObject(ByteBuffer key, ByteBuffer value, String identifier) {
        if(key == null || value == null) return CompletableFuture.completedFuture(false);
        if (!identifier.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name: " + identifier);
        }
        String str = cachekey(key, identifier);
        try {
            cache.put(str, CompletableFuture.completedFuture(value));
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            return CompletableFuture.completedFuture(false);
        }
    }

    @Override
    public CompletableFuture<@Nullable Boolean> removeObject(ByteBuffer key, String identifier) {
        if(key == null) return CompletableFuture.completedFuture(false);
        if (!identifier.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name: " + identifier);
        }
        String str = cachekey(key, identifier);
        try {
            cache.synchronous().invalidate(str);
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            return CompletableFuture.completedFuture(false);
        }
    }

    private String cachekey(ByteBuffer buffer, String id) {
        ByteBuffer full = addIdentifier(buffer, id);;
        byte[] bytes = new byte[full.remaining()];;
        full.slice().get(bytes);
        return Base64.getEncoder().encodeToString(bytes);
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
