package rs.jamie.luneth.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation for serialization fields.
 * Notes:
 * - key can only be defined by 1 field
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface LunethField {

    boolean key() default false;

    boolean nullable() default false;

    int id() default 0;

    int version() default 1; // Currently unused but will be implemented

}
