package rs.jamie.luneth.objects;

import rs.jamie.luneth.StorageSerializer;

import java.nio.ByteBuffer;

public class StorageCharacter implements StorageSerializer<Character> {

    @Override
    public byte[] set(Character object) {
        char ch = object;
        return new byte[] {
                (byte)(ch >> 8),
                (byte)(ch)
        };
    }

    @Override
    public Character get(ByteBuffer buffer) {
        int value = 0;
        value |= (buffer.get() & 0xFF) << 8;
        value |= (buffer.get() & 0xFF);
        return (char) value;
    }
}