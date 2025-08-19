package rs.jamie.luneth.objects;

import rs.jamie.luneth.StorageSerializer;

import java.nio.ByteBuffer;

public class StorageFloat implements StorageSerializer<Float> {

    @Override
    public byte[] set(Float object) {
        int bits = Float.floatToRawIntBits(object);
        return new byte[] {
                (byte)(bits >> 24),
                (byte)(bits >> 16),
                (byte)(bits >> 8),
                (byte)(bits)
        };
    }

    @Override
    public Float get(ByteBuffer buffer) {
        int bits = 0;
        bits |= (buffer.get() & 0xFF) << 24;
        bits |= (buffer.get() & 0xFF) << 16;
        bits |= (buffer.get() & 0xFF) << 8;
        bits |= (buffer.get() & 0xFF);
        return Float.intBitsToFloat(bits);
    }
}