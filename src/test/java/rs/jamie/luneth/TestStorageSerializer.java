package rs.jamie.luneth;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class TestStorageSerializer implements StorageSerializer<Integer, String> {

    LunethManager manager;

    public TestStorageSerializer(LunethManager manager) {
        this.manager = manager;
    }

    @Override
    public String getIdentifier() {
        return "LunethTestObject";
    }

    @Override
    public ByteBuffer encodeKey(Integer key) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(key);
        return buffer.flip();
    }

    @Override
    public ByteBuffer encodeValue(String value) {
        byte[] val = value.getBytes(StandardCharsets.UTF_8);
        int size = val.length;
        ByteBuffer buffer = ByteBuffer.allocate(size + Integer.BYTES);
        buffer.putInt(size);
        buffer.put(val);
        return buffer.flip();
    }

    @Override
    public Integer decodeKey(ByteBuffer buffer) {
        if(buffer==null) return null;
        return buffer.getInt();
    }

    @Override
    public String decodeValue(ByteBuffer buffer) {
        if(buffer==null) return null;
        int size = buffer.getInt();
        byte[] val = new byte[size];
        buffer.get(val, 0, size);
        return new String(val);
    }

}
