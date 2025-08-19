package rs.jamie.luneth.objects;

import rs.jamie.luneth.StorageSerializer;

import java.nio.ByteBuffer;
import java.time.Duration;

public class StorageDuration implements StorageSerializer<Duration> {

    @Override
    public byte[] set(Duration object) {
        long seconds = object.getSeconds();
        int nanos = object.getNano();

        byte[] result = new byte[12];

        for (int i = 0; i < 8; i++) {
            result[i] = (byte)(seconds >> (56 - 8 * i));
        }

        result[8]  = (byte)(nanos >> 24);
        result[9]  = (byte)(nanos >> 16);
        result[10] = (byte)(nanos >> 8);
        result[11] = (byte)(nanos);

        return result;
    }

    @Override
    public Duration get(ByteBuffer buffer) {
        long seconds = 0;
        for (int i = 0; i < 8; i++) {
            seconds = (seconds << 8) | (buffer.get() & 0xFF);
        }

        int nanos = 0;
        for (int i = 0; i < 4; i++) {
            nanos = (nanos << 8) | (buffer.get() & 0xFF);
        }

        return Duration.ofSeconds(seconds, nanos);
    }
}
