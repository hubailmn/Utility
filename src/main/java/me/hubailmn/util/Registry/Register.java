package me.hubailmn.util.Registry;

import me.hubailmn.util.BasePlugin;
import me.hubailmn.util.annotation.LoadConfig;
import me.hubailmn.util.annotation.RegisterEventListener;
import me.hubailmn.util.config.ConfigBuilder;
import me.hubailmn.util.config.ConfigUtil;
import me.hubailmn.util.database.DBConnection;
import me.hubailmn.util.database.DBTable;
import me.hubailmn.util.interaction.CSend;
import org.bukkit.command.TabExecutor;
import org.bukkit.event.Listener;
import org.reflections.Reflections;

import java.util.Set;

public class Register {

    private static final String BASE_PACKAGE = "me.hubailmn." + BasePlugin.getPluginName().toLowerCase();

    private Register() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    public static void eventsListener() {
        Reflections reflections = new Reflections(BASE_PACKAGE + ".listener");

        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(RegisterEventListener.class);

        for (Class<?> annotatedClass : annotatedClasses) {
            if (!annotatedClass.isAnnotationPresent(RegisterEventListener.class)) continue;
            try {
                Listener listener = (Listener) annotatedClass.getDeclaredConstructor().newInstance();
                BasePlugin.getPluginManager().registerEvents(listener, BasePlugin.getInstance());
            } catch (Exception e) {
                CSend.error("Failed to register listener: " + annotatedClass.getSimpleName());
                throw new RuntimeException(e);
            }
        }
    }

    public static void commands() {
        Reflections reflections = new Reflections(BASE_PACKAGE + ".command");
        Set<Class<? extends TabExecutor>> commandClasses = reflections.getSubTypesOf(TabExecutor.class);

        for (Class<? extends TabExecutor> clazz : commandClasses) {
            try {
                TabExecutor executor = clazz.getDeclaredConstructor().newInstance();
                String commandName = clazz.getSimpleName().toLowerCase().replace("command", "");

                CommandRegistry.registerCommand(commandName, executor);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void database() {
        DBConnection.initialize();

        Reflections reflections = new Reflections(BASE_PACKAGE + ".database");
        Set<Class<? extends DBTable>> commandClasses = reflections.getSubTypesOf(DBTable.class);

        for (Class<? extends DBTable> clazz : commandClasses) {
            try {
                DBTable table = clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void config() {
        Reflections reflections = new Reflections(
                BASE_PACKAGE + ".config",
                BASE_PACKAGE + ".util.config.file");

        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(LoadConfig.class);

        for (Class<?> clazz : annotatedClasses) {
            if (!ConfigBuilder.class.isAssignableFrom(clazz)) continue;

            @SuppressWarnings("unchecked")
            Class<? extends ConfigBuilder> typedClass = (Class<? extends ConfigBuilder>) clazz;

            try {
                ConfigBuilder instance = typedClass.getDeclaredConstructor().newInstance();
                ConfigUtil.getCONFIG_INSTANCE().put(typedClass, instance);
            } catch (Exception e) {
                CSend.error("Failed to load config: " + typedClass.getSimpleName());
                throw new RuntimeException(e);
            }
        }
    }

}
