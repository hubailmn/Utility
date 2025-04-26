package me.hubailmn.util.Registry;

import me.hubailmn.util.BasePlugin;
import me.hubailmn.util.annotation.IgnoreFile;
import me.hubailmn.util.annotation.RegisterListener;
import me.hubailmn.util.command.CommandBuilder;
import me.hubailmn.util.command.annotation.Command;
import me.hubailmn.util.config.ConfigBuilder;
import me.hubailmn.util.config.ConfigUtil;
import me.hubailmn.util.config.annotation.LoadConfig;
import me.hubailmn.util.database.DataBaseConnection;
import me.hubailmn.util.database.TableBuilder;
import me.hubailmn.util.database.annotation.DataBaseTable;
import me.hubailmn.util.interaction.CSend;
import org.bukkit.event.Listener;
import org.reflections.Reflections;

import java.util.Set;

public final class Register {

    private static final String BASE_PACKAGE = "me.hubailmn." + BasePlugin.getPluginName().toLowerCase();
    private static final String UTIL_PACKAGE = "me.hubailmn.util";

    private Register() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Registers all event listeners in the project that are annotated with @RegisterListener.
     * Scans through specified packages for classes implementing Listener interface.
     * Validates that annotated classes properly implement the Listener interface.
     */
    public static void eventsListener() {
        scanAndRegister(new Reflections(
                BASE_PACKAGE + ".listener",
                UTIL_PACKAGE + ".menu.listener",
                UTIL_PACKAGE + ".interaction"
        ).getTypesAnnotatedWith(RegisterListener.class), "Event Listener", clazz -> {
            if (!Listener.class.isAssignableFrom(clazz)) {
                CSend.error("Class " + clazz.getName() + " is annotated with @EventListener but does not implement Listener.");
                return;
            }

            Listener listener = (Listener) clazz.getDeclaredConstructor().newInstance();
            BasePlugin.getPluginManager().registerEvents(listener, BasePlugin.getInstance());
            CSend.debug("Registered listener: " + clazz.getSimpleName());
        });
    }

    /**
     * Registers all commands in the project that are annotated with @Command.
     * Scans through command package for classes extending CommandBuilder.
     * Validates that annotated classes properly extend CommandBuilder.
     */
    public static void commands() {
        scanAndRegister(new Reflections(
                BASE_PACKAGE + ".command"
        ).getTypesAnnotatedWith(Command.class), "Command", clazz -> {
            if (!CommandBuilder.class.isAssignableFrom(clazz)) {
                CSend.warn(clazz.getName() + " is annotated with @Command but does not extend BaseCommand.");
                return;
            }

            CommandBuilder executor = (CommandBuilder) clazz.getDeclaredConstructor().newInstance();
            String commandName = clazz.getAnnotation(Command.class).name();
            CommandRegistry.registerCommand(commandName, executor);
            CSend.debug("Registered command: " + commandName);
        });
    }

    /**
     * Initializes database connection and registers all database tables.
     * Scans for classes extending TableBuilder and annotated with @DataBaseTable.
     * Creates instances of found table classes to register them in the database.
     */
    public static void database() {
        DataBaseConnection.initialize();

        scanAndRegister(new Reflections(
                BASE_PACKAGE + ".database"
        ).getSubTypesOf(TableBuilder.class), "Database Table", tableClass -> {
            if (!tableClass.isAnnotationPresent(DataBaseTable.class)) {
                CSend.error(tableClass.getName() + " extends DBTable but is missing @DataBaseTable.");
                return;
            }

            tableClass.getDeclaredConstructor().newInstance();
            CSend.debug("Registered database table: " + tableClass.getSimpleName());
        });
    }

    /**
     * Loads and registers all configuration files for the plugin.
     * Scans for classes extending ConfigBuilder and annotated with @LoadConfig.
     * Handles @IgnoreFile annotations for conditional loading based on database and license status.
     * Stores configurations in the ConfigUtil registry.
     */
    public static void config() {
        scanAndRegister(new Reflections(
                UTIL_PACKAGE + ".config.file",
                BASE_PACKAGE + ".config"
        ).getTypesAnnotatedWith(LoadConfig.class), "Config", clazz -> {
            if (!ConfigBuilder.class.isAssignableFrom(clazz)) {
                CSend.warn(clazz.getName() + " is annotated with @LoadConfig but does not extend ConfigBuilder.");
                return;
            }

            @SuppressWarnings("unchecked") Class<? extends ConfigBuilder> typedClass = (Class<? extends ConfigBuilder>) clazz;

            if (clazz.isAnnotationPresent(IgnoreFile.class)) {
                IgnoreFile ignore = clazz.getAnnotation(IgnoreFile.class);
                if ((ignore.ifNoDatabase() && !BasePlugin.isDatabase()) || (ignore.ifNoLicense() && !BasePlugin.isLicense())) {
                    CSend.debug("Skipping config " + clazz.getSimpleName() + " due to @IgnoreFile conditions.");
                    return;
                }
            }

            ConfigBuilder config = typedClass.getDeclaredConstructor().newInstance();
            ConfigUtil.getCONFIG_INSTANCE().put(typedClass, config);
            CSend.debug("Registered config: " + clazz.getSimpleName());
        });
    }

    /**
     * Generic method to scan and register classes with error handling.
     *
     * @param classes Set of classes to process
     * @param label   Label for logging purposes
     * @param action  Registration action to perform on each class
     * @param <T>     Type of classes being registered
     */
    private static <T> void scanAndRegister(Set<Class<? extends T>> classes, String label, RegistryAction action) {
        for (Class<?> clazz : classes) {
            try {
                action.execute(clazz);
            } catch (Exception e) {
                CSend.error("Failed to register " + label + ": " + clazz.getSimpleName() + " - " + e.getMessage());
                CSend.error(e);
            }
        }
    }

    @FunctionalInterface
    private interface RegistryAction {
        void execute(Class<?> clazz) throws Exception;
    }
}
