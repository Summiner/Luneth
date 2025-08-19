package rs.jamie.luneth;

import java.nio.ByteBuffer;

public interface StorageSerializer<T> {

    byte[] set(T object);

    T get(ByteBuffer buffer);

}
