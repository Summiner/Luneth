package rs.jamie.luneth;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class LunethSerializers {

    private static final ConcurrentHashMap<Class<?>, StorageSerializer<?>> serializers = new ConcurrentHashMap<>();

    public static StorageSerializer<?> getSerializer(@NotNull Class<?> clazz) {
        return serializers.get(clazz);
    }

    public static void register(@NotNull Class<?> clazz, @NotNull StorageSerializer<?> serializer, boolean silent) {
        if(serializers.containsKey(clazz) || serializers.putIfAbsent(clazz, serializer) != null) {
            throw new IllegalArgumentException("[Luneth] Serializer already registered with this id");
        }
        if(!silent) {
            System.out.println("Registered Serializer: "+clazz.getName()+ " → "+serializer.getClass().getName());
        }
    }

    public static void registerPackage(String path, boolean silent) {
        try (ScanResult scanResult = new ClassGraph()
                .acceptPackages(path)
                .enableClassInfo()
                .ignoreClassVisibility()
                .scan()) {

            List<Class<StorageSerializer>> classes =
                    scanResult.getClassesImplementing(StorageSerializer.class)
                            .loadClasses(StorageSerializer.class);

            for (Class<? extends StorageSerializer> cls : classes) {
                try {
                    StorageSerializer<?> instance = cls.getDeclaredConstructor().newInstance();
                    Class<?> type = getSerializerType(cls);
                    if (type == null) {
                        System.err.println("Could not determine handled type for: " + cls.getName());
                        continue;
                    }
                    register(type, instance, silent);
                } catch (Exception e) {
                    throw new RuntimeException("[Luneth] Failed to instantiate: " + cls.getName(), e);
                }
            }
        }
    }

    private static Class<?> getSerializerType(Class<?> clazz) {
        for (Type type : clazz.getGenericInterfaces()) {
            if (type instanceof ParameterizedType parameterized) {
                if (parameterized.getRawType() == StorageSerializer.class) {
                    Type typeArg = parameterized.getActualTypeArguments()[0];
                    if (typeArg instanceof Class<?> c) {
                        return c;
                    }
                }
            }
        }

        return null;
    }

}
