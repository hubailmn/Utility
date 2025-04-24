package me.hubailmn.util.config;

import lombok.Getter;
import me.hubailmn.util.Registry.Register;
import me.hubailmn.util.interaction.CSend;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ConfigUtil {

    @Getter
    private static final Map<Class<?>, ConfigBuilder> CONFIG_INSTANCE = new HashMap<>();

    private ConfigUtil() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    @SuppressWarnings("unchecked")
    public static <T> T getConfig(Class<T> clazz) {
        return (T) CONFIG_INSTANCE.get(clazz);
    }

    public static void reloadAll() {
        CONFIG_INSTANCE.clear();
        Register.config();
    }

    @SuppressWarnings("unchecked")
    public static <T> void reload(Class<T> clazz) {
        try {
            ConfigBuilder instance = ((Class<? extends ConfigBuilder>) clazz).getDeclaredConstructor().newInstance();
            CONFIG_INSTANCE.put(clazz, instance);
            CSend.info("Reloaded config: " + clazz.getSimpleName());
        } catch (Exception e) {
            CSend.error("Failed to reload config: " + clazz.getSimpleName());
            throw new RuntimeException(e);
        }
    }

    public static void saveAll() {
        for (ConfigBuilder builder : CONFIG_INSTANCE.values()) {
            builder.save();
        }
    }

    public static void save(Class<?> clazz) {
        ConfigBuilder builder = CONFIG_INSTANCE.get(clazz);
        if (builder != null) {
            builder.save();
        }
    }

    public static void unregister(Class<?> clazz) {
        CONFIG_INSTANCE.remove(clazz);
    }

    public static boolean isRegistered(Class<?> clazz) {
        return CONFIG_INSTANCE.containsKey(clazz);
    }

    public static Set<Class<?>> listRegistered() {
        return CONFIG_INSTANCE.keySet();
    }

    public static void reloadAllExcept(Class<?>... exclude) {
        Set<Class<?>> exclusions = Set.of(exclude);
        for (Class<?> clazz : new HashMap<>(CONFIG_INSTANCE).keySet()) {
            if (!exclusions.contains(clazz)) {
                reload(clazz);
            }
        }
    }


}
