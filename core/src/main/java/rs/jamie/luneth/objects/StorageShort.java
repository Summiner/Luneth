package rs.jamie.luneth.objects;

import rs.jamie.luneth.StorageSerializer;

import java.nio.ByteBuffer;

public class StorageShort implements StorageSerializer<Short> {

    @Override
    public byte[] set(Short object) {
        return new byte[] {
                (byte)(object >> 8),
                (byte)(object & 0xFF)
        };
    }

    @Override
    public Short get(ByteBuffer buffer) {
        int value = 0;
        value |= (buffer.get() & 0xFF) << 8;
        value |= (buffer.get() & 0xFF);
        return (short) value;
    }
}