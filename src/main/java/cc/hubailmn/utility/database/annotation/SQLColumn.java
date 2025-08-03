package cc.hubailmn.utility.database.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SQLColumn {
    String name() default "";

    String type();

    boolean primaryKey() default false;

    boolean updatable() default true;
}
