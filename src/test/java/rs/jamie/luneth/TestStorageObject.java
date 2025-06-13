package rs.jamie.luneth;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class TestStorageObject implements StorageObject<Integer, String> {

    private final Integer key;
    private final String value;

    public TestStorageObject() {
        this.key = null;
        this.value = null;
    }

    public TestStorageObject(Integer key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getIdentifier() {
        return "LunethTestObject";
    }

    @Override
    public ByteBuffer encodeKey() {
        if(key==null) return null;
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(key);
        return buffer.flip();
    }

    @Override
    public ByteBuffer encodeValue() {
        if(value==null) return null;
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
