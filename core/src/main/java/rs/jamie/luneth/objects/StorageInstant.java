package rs.jamie.luneth.objects;

import rs.jamie.luneth.StorageSerializer;

import java.nio.ByteBuffer;
import java.time.Instant;

public class StorageInstant implements StorageSerializer<Instant> {

    @Override
    public byte[] set(Instant object) {
        long seconds = object.getEpochSecond();
        int nanos = object.getNano();

        byte[] result = new byte[8 + 4];

        result[0] = (byte)(seconds >> 56);
        result[1] = (byte)(seconds >> 48);
        result[2] = (byte)(seconds >> 40);
        result[3] = (byte)(seconds >> 32);
        result[4] = (byte)(seconds >> 24);
        result[5] = (byte)(seconds >> 16);
        result[6] = (byte)(seconds >> 8);
        result[7] = (byte)(seconds);

        result[8]  = (byte)(nanos >> 24);
        result[9]  = (byte)(nanos >> 16);
        result[10] = (byte)(nanos >> 8);
        result[11] = (byte)(nanos);

        return result;
    }

    @Override
    public Instant get(ByteBuffer buffer) {
        long seconds = 0;
        seconds |= ((long) buffer.get() & 0xFF) << 56;
        seconds |= ((long) buffer.get() & 0xFF) << 48;
        seconds |= ((long) buffer.get() & 0xFF) << 40;
        seconds |= ((long) buffer.get() & 0xFF) << 32;
        seconds |= ((long) buffer.get() & 0xFF) << 24;
        seconds |= ((long) buffer.get() & 0xFF) << 16;
        seconds |= ((long) buffer.get() & 0xFF) << 8;
        seconds |= ((long) buffer.get() & 0xFF);

        int nanos = 0;
        nanos |= (buffer.get() & 0xFF) << 24;
        nanos |= (buffer.get() & 0xFF) << 16;
        nanos |= (buffer.get() & 0xFF) << 8;
        nanos |= (buffer.get() & 0xFF);

        return Instant.ofEpochSecond(seconds, nanos);
    }
}