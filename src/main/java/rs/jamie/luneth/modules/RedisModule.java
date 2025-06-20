package rs.jamie.luneth.modules;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.async.RedisStringAsyncCommands;
import io.lettuce.core.codec.RedisCodec;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class RedisModule implements Module {

    private final RedisAsyncCommands<ByteBuffer, ByteBuffer> redis;
    private static final Charset charSet = StandardCharsets.UTF_8;
    private final int cacheTime;

    public RedisModule(String url, Integer cacheTime) {
        this.redis = RedisClient.create(url).connect(new RedisModuleCodec()).async();
        this.cacheTime = cacheTime;
    }

    @Override
    public CompletableFuture<ByteBuffer> getObject(ByteBuffer key, String identifier) {
        if (!identifier.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name: " + identifier);
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                return redis.get(addIdentifier(key, identifier)).get();
            } catch (Exception ignored) {
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> setObject(ByteBuffer key, ByteBuffer value, String identifier) {
        if (!identifier.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name: " + identifier);
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                if(cacheTime!=0) {
                    redis.setex(addIdentifier(key, identifier), cacheTime, value).get();
                } else {
                    redis.set(addIdentifier(key, identifier), value);
                }
                return true;
            } catch (Exception ignored) {
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> removeObject(ByteBuffer key, String identifier) {
        if (!identifier.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name: " + identifier);
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                redis.del(addIdentifier(key, identifier)).get();
                return true;
            } catch (Exception ignored) {
                return false;
            }
        });
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

    public static class RedisModuleCodec implements RedisCodec<ByteBuffer, ByteBuffer> {

        @Override
        public ByteBuffer decodeKey(ByteBuffer byteBuffer) {
            return byteBuffer;
        }

        @Override
        public ByteBuffer decodeValue(ByteBuffer byteBuffer) {
            return byteBuffer;
        }

        @Override
        public ByteBuffer encodeKey(ByteBuffer buffer) {
            return buffer;
        }

        @Override
        public ByteBuffer encodeValue(ByteBuffer buffer) {
            return buffer;
        }

    }
}
