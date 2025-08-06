package cc.hubailmn.utility.registry;

import cc.hubailmn.utility.database.GenericTableManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseManagerRegistry {
    private static final Map<Class<?>, GenericTableManager<?>> managers = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> GenericTableManager<T> getManager(Class<T> entityClass) {
        return (GenericTableManager<T>) managers.get(entityClass);
    }

    public static <T> void registerManager(Class<?> entityClass, GenericTableManager<?> manager) {
        managers.put(entityClass, manager);
    }

    public static void clearAllCaches() {
        managers.values().forEach(GenericTableManager::clearExpiredCache);
    }

    public static void closeAllManagers() {
        managers.values().forEach(GenericTableManager::close);
        managers.clear();
    }
}