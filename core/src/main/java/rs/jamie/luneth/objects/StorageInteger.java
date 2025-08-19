package rs.jamie.luneth.objects;

import rs.jamie.luneth.StorageSerializer;

import java.nio.ByteBuffer;

public class StorageInteger implements StorageSerializer<Integer> {

    @Override
    public byte[] set(Integer object) {
        return new byte[] {
                (byte)(object >> 24),
                (byte)(object >> 16),
                (byte)(object >> 8),
                (byte)(object & 0xFF)
        };
    }

    @Override
    public Integer get(ByteBuffer buffer) {
        int value = 0;
        value |= (buffer.get() & 0xFF) << 24;
        value |= (buffer.get() & 0xFF) << 16;
        value |= (buffer.get() & 0xFF) << 8;
        value |= (buffer.get() & 0xFF);
        return value;
    }
}