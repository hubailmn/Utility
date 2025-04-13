package me.hubailmn.util.Registry;

import me.hubailmn.util.BasePlugin;
import me.hubailmn.util.annotation.DataBaseTable;
import me.hubailmn.util.annotation.LoadConfig;
import me.hubailmn.util.annotation.RegisterCommand;
import me.hubailmn.util.annotation.RegisterEventListener;
import me.hubailmn.util.command.BaseCommand;
import me.hubailmn.util.config.ConfigBuilder;
import me.hubailmn.util.config.ConfigUtil;
import me.hubailmn.util.database.DBConnection;
import me.hubailmn.util.database.DBTable;
import me.hubailmn.util.interaction.CSend;
import org.bukkit.event.Listener;
import org.reflections.Reflections;

import java.util.Set;

public class Register {

    private static final String BASE_PACKAGE = "me.hubailmn." + BasePlugin.getPluginName().toLowerCase();
    private static final String UTIL_PACKAGE = "me.hubailmn.util";

    private Register() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    public static void eventsListener() {
        Reflections reflections = new Reflections(BASE_PACKAGE + ".listener");
        Set<Class<?>> listenerClasses = reflections.getTypesAnnotatedWith(RegisterEventListener.class);

        for (Class<?> clazz : listenerClasses) {
            try {
                if (!Listener.class.isAssignableFrom(clazz)) {
                    CSend.error("Class " + clazz.getName() + " is annotated with @RegisterEventListener but does not implement Listener.");
                    continue;
                }

                Listener listener = (Listener) clazz.getDeclaredConstructor().newInstance();
                BasePlugin.getPluginManager().registerEvents(listener, BasePlugin.getInstance());

            } catch (Exception e) {
                CSend.error("Failed to register listener: " + clazz.getSimpleName());
                throw new RuntimeException(e);
            }
        }
    }

    public static void commands() {
        Reflections reflections = new Reflections(BASE_PACKAGE + ".command");

        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(RegisterCommand.class);

        for (Class<?> annotatedClass : annotatedClasses) {
            if (!BaseCommand.class.isAssignableFrom(annotatedClass)) {
                System.out.println("Warning: " + annotatedClass.getName() + " is annotated with @RegisterCommand but does not extend BaseCommand.");
                continue;
            }

            try {
                BaseCommand executor = (BaseCommand) annotatedClass.getDeclaredConstructor().newInstance();
                String commandName = annotatedClass.getAnnotation(RegisterCommand.class).name();

                CommandRegistry.registerCommand(commandName, executor);
            } catch (Exception e) {
                throw new RuntimeException("Failed to register command: " + annotatedClass.getName(), e);
            }
        }
    }


    public static void database() {
        DBConnection.initialize();

        Reflections reflections = new Reflections(BASE_PACKAGE + ".database");
        Set<Class<? extends DBTable>> tableClasses = reflections.getSubTypesOf(DBTable.class);

        for (Class<? extends DBTable> tableClass : tableClasses) {
            if (!tableClass.isAnnotationPresent(DataBaseTable.class)) continue;

            try {
                tableClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize DBTable: " + tableClass.getSimpleName(), e);
            }
        }
    }


    public static void config() {
        Reflections reflections = new Reflections(
                UTIL_PACKAGE + ".config.file",
                BASE_PACKAGE + ".config");

        Set<Class<?>> configClasses = reflections.getTypesAnnotatedWith(LoadConfig.class);

        for (Class<?> clazz : configClasses) {
            if (!ConfigBuilder.class.isAssignableFrom(clazz)) continue;

            @SuppressWarnings("unchecked")
            Class<? extends ConfigBuilder> typedClass = (Class<? extends ConfigBuilder>) clazz;

            try {
                ConfigBuilder config = typedClass.getDeclaredConstructor().newInstance();
                ConfigUtil.getCONFIG_INSTANCE().put(typedClass, config);
            } catch (Exception e) {
                CSend.error("Failed to load config: " + typedClass.getSimpleName());
                throw new RuntimeException(e);
            }
        }
    }

}
