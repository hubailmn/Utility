package cc.hubailmn.util.menu.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Menu {

    String title();

    int rows();

    boolean inventoryClickCancelled() default true;

    boolean menuClickCancelled() default true;

}
