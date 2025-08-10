package cc.hubailmn.utility.database;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseRegistry {
    private static final Map<Class<?>, TableManager<?>> managers = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> TableManager<T> getManager(Class<T> entityClass) {
        return (TableManager<T>) managers.get(entityClass);
    }

    public static <T> void registerManager(Class<?> entityClass, TableManager<?> manager) {
        managers.put(entityClass, manager);
    }

    public static void clearAllCaches() {
        managers.values().forEach(TableManager::clearExpiredCache);
    }

    public static void closeAllManagers() {
        managers.values().forEach(TableManager::close);
        managers.clear();
    }
}