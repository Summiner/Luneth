package rs.jamie.luneth.modules;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public interface Module {

    CompletableFuture<ByteBuffer> getObject(ByteBuffer key, String identifier);

    CompletableFuture<Boolean> setObject(ByteBuffer key, ByteBuffer value, String identifier);

    default void createTable(String name) {}

}
