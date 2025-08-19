package rs.jamie.luneth.objects;

import rs.jamie.luneth.StorageSerializer;

import java.nio.ByteBuffer;
import java.time.LocalDate;

public class StorageLocalDate implements StorageSerializer<LocalDate> {

    @Override
    public byte[] set(LocalDate object) {
        int year = object.getYear();
        byte month = (byte) object.getMonthValue();
        byte day = (byte) object.getDayOfMonth();

        return new byte[] {
                (byte)(year >> 24),
                (byte)(year >> 16),
                (byte)(year >> 8),
                (byte)(year),
                month,
                day
        };
    }

    @Override
    public LocalDate get(ByteBuffer buffer) {
        int year = 0;
        year |= (buffer.get() & 0xFF) << 24;
        year |= (buffer.get() & 0xFF) << 16;
        year |= (buffer.get() & 0xFF) << 8;
        year |= (buffer.get() & 0xFF);

        int month = buffer.get() & 0xFF;
        int day = buffer.get() & 0xFF;

        return LocalDate.of(year, month, day);
    }
}