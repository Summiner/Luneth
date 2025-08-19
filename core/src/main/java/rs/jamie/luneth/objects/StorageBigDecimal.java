package rs.jamie.luneth.objects;

import rs.jamie.luneth.StorageSerializer;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;

public class StorageBigDecimal implements StorageSerializer<BigDecimal> {

    @Override
    public byte[] set(BigDecimal object) {
        byte[] unscaledBytes = object.unscaledValue().toByteArray();
        int scale = object.scale();

        byte[] result = new byte[4 + 4 + unscaledBytes.length];
        int len = unscaledBytes.length;

        result[0] = (byte)(len >> 24);
        result[1] = (byte)(len >> 16);
        result[2] = (byte)(len >> 8);
        result[3] = (byte)(len);

        result[4] = (byte)(scale >> 24);
        result[5] = (byte)(scale >> 16);
        result[6] = (byte)(scale >> 8);
        result[7] = (byte)(scale);

        System.arraycopy(unscaledBytes, 0, result, 8, len);
        return result;
    }

    @Override
    public BigDecimal get(ByteBuffer buffer) {
        int len = 0;
        len |= (buffer.get() & 0xFF) << 24;
        len |= (buffer.get() & 0xFF) << 16;
        len |= (buffer.get() & 0xFF) << 8;
        len |= (buffer.get() & 0xFF);

        int scale = 0;
        scale |= (buffer.get() & 0xFF) << 24;
        scale |= (buffer.get() & 0xFF) << 16;
        scale |= (buffer.get() & 0xFF) << 8;
        scale |= (buffer.get() & 0xFF);

        byte[] bytes = new byte[len];
        buffer.get(bytes);

        return new BigDecimal(new BigInteger(bytes), scale);
    }
}