package me.hubailmn.util.Registry;

import me.hubailmn.util.BasePlugin;
import me.hubailmn.util.annotation.EventListener;
import me.hubailmn.util.annotation.IgnoreFile;
import me.hubailmn.util.command.BaseCommand;
import me.hubailmn.util.command.annotation.Command;
import me.hubailmn.util.config.ConfigBuilder;
import me.hubailmn.util.config.ConfigUtil;
import me.hubailmn.util.config.annotation.LoadConfig;
import me.hubailmn.util.database.DBConnection;
import me.hubailmn.util.database.DBTable;
import me.hubailmn.util.database.annotation.DataBaseTable;
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
        Reflections reflections = new Reflections(
                BASE_PACKAGE + ".listener",
                UTIL_PACKAGE + ".menu.listener");
        Set<Class<?>> listenerClasses = reflections.getTypesAnnotatedWith(EventListener.class);

        for (Class<?> clazz : listenerClasses) {
            try {
                if (!Listener.class.isAssignableFrom(clazz)) {
                    CSend.error("Class " + clazz.getName() + " is annotated with @EventListener but does not implement Listener.");
                    continue;
                }

                Listener listener = (Listener) clazz.getDeclaredConstructor().newInstance();
                BasePlugin.getPluginManager().registerEvents(listener, BasePlugin.getInstance());
                CSend.debug("Registered listener " + clazz.getName());

            } catch (Exception e) {
                CSend.error("Failed to register listener: " + clazz.getSimpleName());
                throw new RuntimeException(e);
            }
        }
    }

    public static void commands() {
        Reflections reflections = new Reflections(BASE_PACKAGE + ".command");

        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(Command.class);

        for (Class<?> annotatedClass : annotatedClasses) {
            if (!BaseCommand.class.isAssignableFrom(annotatedClass)) {
                CSend.warn(annotatedClass.getName() + " is annotated with @Command but does not extend BaseCommand.");
                continue;
            }

            try {
                BaseCommand executor = (BaseCommand) annotatedClass.getDeclaredConstructor().newInstance();
                String commandName = annotatedClass.getAnnotation(Command.class).name();

                CommandRegistry.registerCommand(commandName, executor);

                CSend.debug("Registered command " + commandName + ".");

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
            if (!tableClass.isAnnotationPresent(DataBaseTable.class)) {
                CSend.error(tableClass.getName() + " is annotated with @DataBaseTable. but does not extend DBTable.");
                continue;
            }

            try {
                tableClass.getDeclaredConstructor().newInstance();

                CSend.debug("Registered Database table " + tableClass.getSimpleName() + ".");

            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize DBTable: " + tableClass.getSimpleName(), e);
            }
        }
    }


    public static void config() {
        Reflections reflections = new Reflections(
                UTIL_PACKAGE + ".config.file",
                BASE_PACKAGE + ".config"
        );

        Set<Class<?>> configClasses = reflections.getTypesAnnotatedWith(LoadConfig.class);

        for (Class<?> clazz : configClasses) {
            if (!ConfigBuilder.class.isAssignableFrom(clazz)) {
                CSend.warn(clazz.getName() + " is annotated with @LoadConfig but does not extend ConfigBuilder.");
                continue;
            }

            @SuppressWarnings("unchecked")
            Class<? extends ConfigBuilder> typedClass = (Class<? extends ConfigBuilder>) clazz;

            if (clazz.isAnnotationPresent(IgnoreFile.class)) {
                IgnoreFile ignore = clazz.getAnnotation(IgnoreFile.class);

                if ((ignore.ifNoDatabase() && !BasePlugin.isDatabase()) || (ignore.ifNoLicense() && !BasePlugin.isLicense())) {
                    CSend.debug("Skipping config " + clazz.getSimpleName() + " due to @IgnoreFile conditions.");
                    continue;
                }
            }


            try {
                ConfigBuilder config = typedClass.getDeclaredConstructor().newInstance();
                ConfigUtil.getCONFIG_INSTANCE().put(typedClass, config);

                CSend.debug("Registered config " + clazz.getSimpleName() + ".");
            } catch (Exception e) {
                CSend.error("Failed to load config: " + typedClass.getSimpleName());
                throw new RuntimeException(e);
            }
        }
    }


}
