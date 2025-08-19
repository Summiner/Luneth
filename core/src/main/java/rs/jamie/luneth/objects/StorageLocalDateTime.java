package rs.jamie.luneth.objects;

import rs.jamie.luneth.StorageSerializer;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;

public class StorageLocalDateTime implements StorageSerializer<LocalDateTime> {

    @Override
    public byte[] set(LocalDateTime object) {
        byte[] result = new byte[4 + 1 + 1 + 1 + 1 + 1 + 4];
        int year = object.getYear();
        int nanos = object.getNano();

        result[0] = (byte)(year >> 24);
        result[1] = (byte)(year >> 16);
        result[2] = (byte)(year >> 8);
        result[3] = (byte)(year);

        result[4] = (byte) object.getMonthValue();
        result[5] = (byte) object.getDayOfMonth();
        result[6] = (byte) object.getHour();
        result[7] = (byte) object.getMinute();
        result[8] = (byte) object.getSecond();

        result[9]  = (byte)(nanos >> 24);
        result[10] = (byte)(nanos >> 16);
        result[11] = (byte)(nanos >> 8);
        result[12] = (byte)(nanos);

        return result;
    }

    @Override
    public LocalDateTime get(ByteBuffer buffer) {
        int year = 0;
        year |= (buffer.get() & 0xFF) << 24;
        year |= (buffer.get() & 0xFF) << 16;
        year |= (buffer.get() & 0xFF) << 8;
        year |= (buffer.get() & 0xFF);

        int month = buffer.get() & 0xFF;
        int day   = buffer.get() & 0xFF;
        int hour  = buffer.get() & 0xFF;
        int min   = buffer.get() & 0xFF;
        int sec   = buffer.get() & 0xFF;

        int nanos = 0;
        nanos |= (buffer.get() & 0xFF) << 24;
        nanos |= (buffer.get() & 0xFF) << 16;
        nanos |= (buffer.get() & 0xFF) << 8;
        nanos |= (buffer.get() & 0xFF);

        return LocalDateTime.of(year, month, day, hour, min, sec, nanos);
    }
}