package cc.hubailmn.util.Registry;

import cc.hubailmn.util.BasePlugin;
import cc.hubailmn.util.annotation.IgnoreFile;
import cc.hubailmn.util.annotation.RegisterListener;
import cc.hubailmn.util.command.CommandBuilder;
import cc.hubailmn.util.command.annotation.Command;
import cc.hubailmn.util.config.ConfigBuilder;
import cc.hubailmn.util.config.ConfigUtil;
import cc.hubailmn.util.config.annotation.LoadConfig;
import cc.hubailmn.util.database.DataBaseConnection;
import cc.hubailmn.util.database.TableBuilder;
import cc.hubailmn.util.database.annotation.DataBaseTable;
import cc.hubailmn.util.interaction.CSend;
import org.bukkit.event.Listener;

import java.util.Set;

public final class Register {

    private static final String UTIL_PACKAGE = "cc.hubailmn.util";

    private Register() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void eventsListener() {
        String BASE_PACKAGE = BasePlugin.getPluginName();
        scanAndRegister(ReflectionsUtil.build(
                UTIL_PACKAGE + ".listener",
                UTIL_PACKAGE + ".menu.listener",
                UTIL_PACKAGE + ".interaction",
                BASE_PACKAGE + ".listener",
                BASE_PACKAGE + ".menu"
        ).getTypesAnnotatedWith(RegisterListener.class), "Event Listener", clazz -> {
            if (!Listener.class.isAssignableFrom(clazz)) {
                CSend.error("Class " + clazz.getName() + " is annotated with @RegisterListener but does not implement Listener.");
                return;
            }

            Listener listener = (Listener) clazz.getDeclaredConstructor().newInstance();
            BasePlugin.getPluginManager().registerEvents(listener, BasePlugin.getInstance());
            CSend.debug("Registered listener: " + clazz.getSimpleName());
        });
    }

    public static void commands() {
        String BASE_PACKAGE = BasePlugin.getPluginName();
        scanAndRegister(ReflectionsUtil.build(
                BASE_PACKAGE + ".command"
        ).getTypesAnnotatedWith(Command.class), "Command", clazz -> {
            if (!CommandBuilder.class.isAssignableFrom(clazz)) {
                CSend.warn(clazz.getName() + " is annotated with @Command but does not extend BaseCommand.");
                return;
            }

            CommandBuilder executor = (CommandBuilder) clazz.getDeclaredConstructor().newInstance();
            String commandName = clazz.getAnnotation(Command.class).name();
            CommandRegistry.registerCommand(commandName, executor, executor.getAliases());
            CSend.debug("Registered command: " + commandName);
        });
    }

    public static void database() {
        String BASE_PACKAGE = BasePlugin.getPluginName();
        DataBaseConnection.initialize();

        scanAndRegister(ReflectionsUtil.build(
                BASE_PACKAGE + ".database"
        ).getSubTypesOf(TableBuilder.class), "Database Table", tableClass -> {
            if (!tableClass.isAnnotationPresent(DataBaseTable.class)) {
                CSend.error(tableClass.getName() + " extends TableBuilder but is missing @DataBaseTable.");
                return;
            }

            tableClass.getDeclaredConstructor().newInstance();
            CSend.debug("Registered database table: " + tableClass.getSimpleName());
        });
    }

    public static void config() {
        String BASE_PACKAGE = BasePlugin.getPluginName();
        scanAndRegister(ReflectionsUtil.build(
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
                if ((ignore.database() && !BasePlugin.isDatabase()) || (ignore.license() && !BasePlugin.isLicense())) {
                    CSend.debug("Skipping config " + clazz.getSimpleName() + " due to @IgnoreFile conditions.");
                    return;
                }
            }

            ConfigBuilder config = typedClass.getDeclaredConstructor().newInstance();
            ConfigUtil.getCONFIG_INSTANCE().put(typedClass, config);
            CSend.debug("Registered config: " + clazz.getSimpleName());
        });
    }

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