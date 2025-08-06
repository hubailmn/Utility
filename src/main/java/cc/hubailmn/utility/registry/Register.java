package cc.hubailmn.utility.registry;

import cc.hubailmn.utility.BasePlugin;
import cc.hubailmn.utility.annotation.IgnoreFile;
import cc.hubailmn.utility.annotation.RegisterListener;
import cc.hubailmn.utility.command.CommandBuilder;
import cc.hubailmn.utility.command.annotation.Command;
import cc.hubailmn.utility.config.ConfigBuilder;
import cc.hubailmn.utility.config.ConfigUtil;
import cc.hubailmn.utility.config.annotation.LoadConfig;
import cc.hubailmn.utility.database.DataBaseConnection;
import cc.hubailmn.utility.database.GenericTableManager;
import cc.hubailmn.utility.database.annotation.DataBaseTable;
import cc.hubailmn.utility.interaction.CSend;
import org.bukkit.event.Listener;

import java.sql.Connection;
import java.util.*;

public final class Register {

    private static final String BASE_PACKAGE = BasePlugin.getInstance().getPackageName();
    private static final String UTIL_PACKAGE = "cc.hubailmn.utility";

    private Register() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void eventsListener() {
        scanAndRegister(ReflectionsUtil.build(
                UTIL_PACKAGE + ".listener",
                BASE_PACKAGE + ".listener",
                BASE_PACKAGE + ".menu"
        ).getTypesAnnotatedWith(RegisterListener.class), "Event Listener", clazz -> {
            if (!Listener.class.isAssignableFrom(clazz)) {
                CSend.error("Class " + clazz.getName() + " is annotated with @RegisterListener but does not implement Listener.");
                return;
            }

            Listener listener = (Listener) clazz.getDeclaredConstructor().newInstance();
            BasePlugin.getInstance().getPluginManager().registerEvents(listener, BasePlugin.getInstance());
            CSend.debug("Registered listener: " + clazz.getSimpleName());
        });
    }

    public static void commands() {
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
        });
    }

    public static void database() {
        DataBaseConnection.initialize();
        Connection conn = DataBaseConnection.getConnection();

        List<GenericTableManager<?>> managers = new ArrayList<>();

        scanAndRegister(ReflectionsUtil.build(
                BASE_PACKAGE + ".database"
        ).getTypesAnnotatedWith(DataBaseTable.class), "Database Table", entityClass -> {
            try {
                GenericTableManager<?> manager = new GenericTableManager<>(entityClass, conn);
                manager.createTable();
                managers.add(manager);
                DatabaseManagerRegistry.registerManager(entityClass, manager);

                CSend.debug("Registered database table for entity: " + entityClass.getSimpleName());
            } catch (Exception e) {
                CSend.error("Failed to register database table for entity: " + entityClass.getName());
                CSend.error(e);
            }
        });

        schedulePeriodicCacheCleanup(managers);
    }

    private static void schedulePeriodicCacheCleanup(List<GenericTableManager<?>> managers) {
        Timer cacheCleanupTimer = new Timer("DatabaseCacheCleanup", true);
        cacheCleanupTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (GenericTableManager<?> manager : managers) {
                    try {
                        manager.clearExpiredCache();
                    } catch (Exception e) {
                        CSend.error("Error during cache cleanup", e);
                    }
                }
            }
        }, 300000, 300000);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            cacheCleanupTimer.cancel();
            for (GenericTableManager<?> manager : managers) {
                manager.close();
            }
        }));
    }

    public static void config() {
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
                if ((ignore.database() && !BasePlugin.getInstance().isDatabase()) || (ignore.license() && !BasePlugin.getInstance().isLicense()) || (ignore.discord() && !BasePlugin.getInstance().isDiscord())) {
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