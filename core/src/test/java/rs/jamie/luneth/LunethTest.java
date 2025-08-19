package rs.jamie.luneth;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import rs.jamie.luneth.annotations.LunethField;
import rs.jamie.luneth.annotations.LunethSerializer;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class LunethTest {

    private static LunethManager manager;

    @BeforeAll
    public static void setup() {
        manager = new LunethManager.Builder()
                .setStorageMode(LunethManager.StorageModes.CAFFEINE)
                .build();

        System.out.println("Passed manager initialization");
    }

    @Test
    public void testValidInput() {
        UUID uuid = UUID.randomUUID();
        TestStorageObject input = new TestStorageObject(uuid, "Summiner", 125.5);

        boolean success = manager.put(input).join();
        assertTrue(success, "Put should succeed");

        TestStorageObject output = manager.get(TestStorageObject.class, uuid).join();
        assertNotNull(output, "Output object should not be null");
        System.out.println("Passed valid input test with result: "+output);
    }

    @Test
    public void testNullKey() {
        UUID randomId = UUID.randomUUID();
        TestStorageObject result = manager.get(TestStorageObject.class, randomId).join();
        assertNull(result, "Getting a missing key should return null");
        System.out.println("Passed null key test");
    }

    @Test
    public void testNullValue() {
        UUID randomId = UUID.randomUUID();
        TestStorageObject result = manager.get(TestStorageObject.class, randomId).join();
        assertNull(result, "Getting a missing value should return null");
        System.out.println("Passed null value test");
    }

    @Test
    public void testRemovedObject() {
        UUID randomId = UUID.randomUUID();
        Boolean res1 = manager.put(new TestStorageObject(randomId, "RemovedObjectTest", 10.5)).join();
        assertEquals(true, res1, "Storing this object shouldn't error");
        System.out.println(manager.get(TestStorageObject.class, randomId).join());
        Boolean res2 = manager.remove(TestStorageObject.class, randomId).join();
        assertEquals(true, res2, "Removing this object shouldn't error");
        System.out.println(manager.get(TestStorageObject.class, randomId).join());
    }

    // Example Code

    public static void main(String[] args) {
        LunethManager manager = new LunethManager.Builder()
                .setStorageMode(LunethManager.StorageModes.CAFFEINE)
                .build();

        UUID uuid = UUID.randomUUID();
        ExampleObject object = new ExampleObject(uuid, "Example");

        System.out.println(manager.put(object).join()); // Write object to Luneth
        System.out.println(manager.get(ExampleObject.class, uuid).join()); // Read object from Luneth
    }

    @LunethSerializer(identifier = "ExampleObject")
    public static class ExampleObject implements StorageObject {
        @LunethField(key = true)
        public final UUID player;

        @LunethField(id = 1)
        public final String username;

        public ExampleObject(UUID player, String username) {
            this.player = player;
            this.username = username;
        }

        public String toString() {
            return "ExampleObject{UUID player="+player+", String username="+username+"}";
        }
    }


}
