package me.hubailmn.util.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SubCommand {

    String name();

    String permission() default "";

    boolean requiresPlayer() default false;

    Class<?> baseCommand();


}
