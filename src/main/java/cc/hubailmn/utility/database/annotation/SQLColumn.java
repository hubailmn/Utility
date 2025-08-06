package cc.hubailmn.utility.database.annotation;

import cc.hubailmn.utility.database.data.SQLType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SQLColumn {
    String name() default "";

    SQLType type();

    boolean primaryKey() default false;

    boolean updatable() default true;

    boolean nullable() default true;

    boolean autoIncrement() default false;

    boolean index() default false;

    boolean uniqueIndex() default false;

    boolean foreignKey() default false;

    String references() default "";

    String defaultValue() default "";
}
