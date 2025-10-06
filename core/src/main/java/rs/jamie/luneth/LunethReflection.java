package rs.jamie.luneth;

import org.jetbrains.annotations.NotNull;
import rs.jamie.luneth.annotations.LunethField;
import rs.jamie.luneth.annotations.LunethSerializer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LunethReflection {

    public static String getIdentifier(StorageObject object) {
        Class<?> clazz = object.getClass();
        if (clazz.isAnnotationPresent(LunethSerializer.class)) {
            LunethSerializer lunethSerializer = clazz.getAnnotation(LunethSerializer.class);
            return lunethSerializer.identifier();
        }

        throw new IllegalStateException("Class " + clazz.getName() + " must be annotated with @LunethSerializer");
    }

    public static String getIdentifier(Class<? extends StorageObject> clazz) {
        if (clazz.isAnnotationPresent(LunethSerializer.class)) {
            LunethSerializer lunethSerializer = clazz.getAnnotation(LunethSerializer.class);
            return lunethSerializer.identifier();
        }

        throw new IllegalStateException("Class " + clazz.getName() + " must be annotated with @LunethSerializer");
    }

    public static Field getKeyField(StorageObject object) {
        Class<?> clazz = object.getClass();
        return getField(clazz.getDeclaredFields(), clazz.getName(), clazz);
    }

    public static Field getKeyField(Class<? extends StorageObject> clazz) {
        return getField(clazz.getDeclaredFields(), clazz.getName(), clazz);
    }

    @NotNull
    private static Field getField(Field[] declaredFields, String name, Class<?> clazz) {
        for (Field field : declaredFields) {
            LunethField annotation = field.getAnnotation(LunethField.class);
            if (annotation != null && annotation.key()) {
                field.setAccessible(true);
                return field;
            }
        }

        throw new IllegalStateException("@LunethField: No key field found in "+ name);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getValueObject(Field field, StorageObject object) {
        try {
            return (T) field.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access key field in " + object.getClass().getName(), e);
        }
    }


    public static List<Field> getValueFields(StorageObject object) {
        return getValueFields(object.getClass());
    }

    public static List<Field> getValueFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(LunethField.class)) {
                if(!field.getAnnotation(LunethField.class).key()) {
                    fields.add(field);
                }
            }
        }
        fields.sort(Comparator.comparingInt(f -> f.getAnnotation(LunethField.class).id()));
        return fields;
    }


}
