package rs.jamie.luneth.objects;

import rs.jamie.luneth.StorageSerializer;

import java.math.BigInteger;
import java.nio.ByteBuffer;

public class StorageBigInteger implements StorageSerializer<BigInteger> {

    @Override
    public byte[] set(BigInteger object) {
        byte[] valueBytes = object.toByteArray();
        byte[] result = new byte[4 + valueBytes.length];
        int length = valueBytes.length;

        result[0] = (byte)(length >> 24);
        result[1] = (byte)(length >> 16);
        result[2] = (byte)(length >> 8);
        result[3] = (byte)(length);

        System.arraycopy(valueBytes, 0, result, 4, length);
        return result;
    }

    @Override
    public BigInteger get(ByteBuffer buffer) {
        int length = 0;
        length |= (buffer.get() & 0xFF) << 24;
        length |= (buffer.get() & 0xFF) << 16;
        length |= (buffer.get() & 0xFF) << 8;
        length |= (buffer.get() & 0xFF);

        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return new BigInteger(bytes);
    }
}