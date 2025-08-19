package rs.jamie.luneth.objects;

import rs.jamie.luneth.StorageSerializer;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StorageString implements StorageSerializer<String> {

    private final Charset CHARSET = StandardCharsets.UTF_8;

    @Override
    public byte[] set(String object) {
        byte[] string = object != null ? object.getBytes(CHARSET) : new byte[]{};
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + string.length);
        buffer.putInt(string.length);
        buffer.put(string);
        return buffer.flip().array();
    }

    @Override
    public String get(ByteBuffer buffer) {
        int size = buffer.getInt();
        if(size == 0) return null;
        byte[] string = new byte[size];
        buffer.get(string, 0, size);
        return new String(string);
    }
}
