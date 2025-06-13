package rs.jamie.luneth;

import java.nio.ByteBuffer;

public interface StorageObject<K, V> {

    String getIdentifier();

    ByteBuffer encodeKey();

    ByteBuffer encodeValue();

    K decodeKey(ByteBuffer buffer);

    V decodeValue(ByteBuffer buffer);

}
