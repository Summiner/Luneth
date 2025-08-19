package rs.jamie.luneth.objects;

import rs.jamie.luneth.StorageSerializer;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class StorageOffsetDateTime implements StorageSerializer<OffsetDateTime> {

    @Override
    public byte[] set(OffsetDateTime object) {
        Instant instant = object.toInstant();
        int offsetSeconds = object.getOffset().getTotalSeconds();

        byte[] result = new byte[8 + 4 + 4];

        long seconds = instant.getEpochSecond();
        int nanos = instant.getNano();

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

        result[12] = (byte)(offsetSeconds >> 24);
        result[13] = (byte)(offsetSeconds >> 16);
        result[14] = (byte)(offsetSeconds >> 8);
        result[15] = (byte)(offsetSeconds);

        return result;
    }

    @Override
    public OffsetDateTime get(ByteBuffer buffer) {
        long seconds = 0;
        for (int i = 0; i < 8; i++) {
            seconds = (seconds << 8) | (buffer.get() & 0xFF);
        }

        int nanos = 0;
        for (int i = 0; i < 4; i++) {
            nanos = (nanos << 8) | (buffer.get() & 0xFF);
        }

        int offsetSeconds = 0;
        for (int i = 0; i < 4; i++) {
            offsetSeconds = (offsetSeconds << 8) | (buffer.get() & 0xFF);
        }

        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(seconds, nanos), ZoneOffset.ofTotalSeconds(offsetSeconds));
    }
}
