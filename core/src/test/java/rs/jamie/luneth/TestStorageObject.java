package rs.jamie.luneth;

import rs.jamie.luneth.annotations.LunethField;
import rs.jamie.luneth.annotations.LunethSerializer;

import java.util.UUID;

@LunethSerializer(identifier = "LunethTestObject")
public class TestStorageObject implements StorageObject {

    @LunethField(key = true)
    public final UUID player;

    @LunethField(id = 1)
    public final String username;

    @LunethField(id = 2)
    public final Double balance;

    public TestStorageObject(UUID player, String username, Double balance) {
        this.player = player;
        this.username = username;
        this.balance = balance;
    }

    public String toString() {
        return "TestStorageObject{UUID player="+player+", String username="+username+", Double balance="+balance+"}";
    }
}
