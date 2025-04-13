package me.hubailmn.util.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RegisterCommand {

    String name();

    String description() default "";

    String usage() default "";

    String permission() default "";

    String[] aliases() default {};

    SubCommand[] subcommands() default {};


}
