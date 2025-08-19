package rs.jamie.luneth.objects;

import rs.jamie.luneth.StorageSerializer;

import java.nio.ByteBuffer;
import java.util.UUID;

public class StorageUUID implements StorageSerializer<UUID> {

    @Override
    public byte[] set(UUID object) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * 2);
        buffer.putLong(object.getMostSignificantBits());
        buffer.putLong(object.getLeastSignificantBits());
        return buffer.flip().array();
    }

    @Override
    public UUID get(ByteBuffer buffer) {
        return new UUID(buffer.getLong(), buffer.getLong());
    }

}
