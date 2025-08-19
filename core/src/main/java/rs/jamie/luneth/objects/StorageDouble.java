package rs.jamie.luneth.objects;

import rs.jamie.luneth.StorageSerializer;

import java.nio.ByteBuffer;

public class StorageDouble implements StorageSerializer<Double> {

    @Override
    public byte[] set(Double object) {
        long bits = Double.doubleToRawLongBits(object);
        return new byte[] {
                (byte)(bits >> 56),
                (byte)(bits >> 48),
                (byte)(bits >> 40),
                (byte)(bits >> 32),
                (byte)(bits >> 24),
                (byte)(bits >> 16),
                (byte)(bits >> 8),
                (byte)(bits & 0xFF)
        };
    }

    @Override
    public Double get(ByteBuffer buffer) {
        long bits = 0;
        bits |= ((long) buffer.get() & 0xFF) << 56;
        bits |= ((long) buffer.get() & 0xFF) << 48;
        bits |= ((long) buffer.get() & 0xFF) << 40;
        bits |= ((long) buffer.get() & 0xFF) << 32;
        bits |= ((long) buffer.get() & 0xFF) << 24;
        bits |= ((long) buffer.get() & 0xFF) << 16;
        bits |= ((long) buffer.get() & 0xFF) << 8;
        bits |= ((long) buffer.get() & 0xFF);
        return Double.longBitsToDouble(bits);
    }
}
