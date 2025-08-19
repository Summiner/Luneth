package rs.jamie.luneth.objects;

import rs.jamie.luneth.StorageSerializer;

import java.nio.ByteBuffer;

public class StorageBoolean implements StorageSerializer<Boolean> {

    @Override
    public byte[] set(Boolean object) {
        return new byte[] { (byte) (object ? 1 : 0) };
    }

    @Override
    public Boolean get(ByteBuffer buffer) {
        return buffer.get() != 0;
    }
}