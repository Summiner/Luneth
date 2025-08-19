package rs.jamie.luneth.objects;

import rs.jamie.luneth.StorageSerializer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class StorageZonedDateTime implements StorageSerializer<ZonedDateTime> {

    @Override
    public byte[] set(ZonedDateTime object) {
        Instant instant = object.toInstant();
        String zoneId = object.getZone().getId();
        byte[] instantBytes = new StorageInstant().set(instant);
        byte[] zoneBytes = zoneId.getBytes(StandardCharsets.UTF_8);

        byte[] result = new byte[instantBytes.length + 4 + zoneBytes.length];

        System.arraycopy(instantBytes, 0, result, 0, instantBytes.length);

        int len = zoneBytes.length;
        int offset = instantBytes.length;
        result[offset] = (byte)(len >> 24);
        result[offset + 1] = (byte)(len >> 16);
        result[offset + 2] = (byte)(len >> 8);
        result[offset + 3] = (byte)(len);

        System.arraycopy(zoneBytes, 0, result, offset + 4, len);

        return result;
    }

    @Override
    public ZonedDateTime get(ByteBuffer buffer) {
        Instant instant = new StorageInstant().get(buffer);

        int len = 0;
        len |= (buffer.get() & 0xFF) << 24;
        len |= (buffer.get() & 0xFF) << 16;
        len |= (buffer.get() & 0xFF) << 8;
        len |= (buffer.get() & 0xFF);

        byte[] zoneBytes = new byte[len];
        buffer.get(zoneBytes);
        String zoneId = new String(zoneBytes, StandardCharsets.UTF_8);

        return ZonedDateTime.ofInstant(instant, ZoneId.of(zoneId));
    }
}
