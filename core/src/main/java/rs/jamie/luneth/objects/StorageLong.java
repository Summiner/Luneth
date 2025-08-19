package rs.jamie.luneth.objects;

import rs.jamie.luneth.StorageSerializer;

import java.nio.ByteBuffer;

public class StorageLong implements StorageSerializer<Long> {

    @Override
    public byte[] set(Long object) {
        return new byte[] {
                (byte)(object >> 56),
                (byte)(object >> 48),
                (byte)(object >> 40),
                (byte)(object >> 32),
                (byte)(object >> 24),
                (byte)(object >> 16),
                (byte)(object >> 8),
                (byte)(object & 0xFF)
        };
    }

    @Override
    public Long get(ByteBuffer buffer) {
        long value = 0;
        value |= ((long) buffer.get() & 0xFF) << 56;
        value |= ((long) buffer.get() & 0xFF) << 48;
        value |= ((long) buffer.get() & 0xFF) << 40;
        value |= ((long) buffer.get() & 0xFF) << 32;
        value |= ((long) buffer.get() & 0xFF) << 24;
        value |= ((long) buffer.get() & 0xFF) << 16;
        value |= ((long) buffer.get() & 0xFF) << 8;
        value |= ((long) buffer.get() & 0xFF);
        return value;
    }
}