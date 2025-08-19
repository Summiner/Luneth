package rs.jamie.luneth.objects;

import rs.jamie.luneth.StorageSerializer;

import java.nio.ByteBuffer;

public class StorageByte implements StorageSerializer<Byte> {

    @Override
    public byte[] set(Byte object) {
        return new byte[] { object };
    }

    @Override
    public Byte get(ByteBuffer buffer) {
        return buffer.get();
    }
}