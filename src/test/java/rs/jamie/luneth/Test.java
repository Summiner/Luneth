package rs.jamie.luneth;

public class Test {

    public static void main(String[] args) {
        LunethManager manager = new LunethManager.Builder()
                .setStorageMode(LunethManager.StorageModes.CAFFEINE)
                .build();
        TestStorageSerializer object = new TestStorageSerializer(manager);

        System.out.println(manager.setObject(object, 12, "Test").join());
        System.out.println(manager.getObject(object, 12).join());
        System.out.println(manager.removeObject(object, 12).join());
        System.out.println(manager.getObject(object, 12).join());
    }

}
