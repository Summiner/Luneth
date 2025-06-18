package rs.jamie.luneth;

import java.nio.ByteBuffer;

public interface StorageSerializer<K, V> {

    String getIdentifier();

    ByteBuffer encodeKey(K key);

    ByteBuffer encodeValue(V value);

    K decodeKey(ByteBuffer buffer);

    V decodeValue(ByteBuffer buffer);

}
