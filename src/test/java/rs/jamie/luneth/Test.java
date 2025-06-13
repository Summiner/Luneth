package rs.jamie.luneth;

public class Test {

    public static void main(String[] args) {
        LunethManager lunethManager = new LunethManager.Builder()
                .setStorageMode(LunethManager.StorageModes.SQL)
                .setConnectionURL("jdbc:h2:D:/Code/Luneth/build/storage")
                .build();
        TestStorageObject object = new TestStorageObject(69, "Hello");

        System.out.println(lunethManager.setObject(object).join());
        System.out.println(object.decodeKey(object.encodeKey())+" | "+ lunethManager.getObject(object).join());
    }

}
