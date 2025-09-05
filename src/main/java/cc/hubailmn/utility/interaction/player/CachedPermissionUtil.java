package cc.hubailmn.utility.interaction.player;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class CachedPermissionUtil {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");

    private static final Cache<String, Integer> NUMBER_CACHE = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build();

    private static final Cache<String, String> STRING_CACHE = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build();

    private static final Cache<String, Object> GENERIC_CACHE = Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build();

    /**
     * Uncached version for internal use to avoid cache recursion
     */
    private static int getPermissionNumberUncached(Player player, String permissionPrefix, int defaultValue) {
        return player.getEffectivePermissions().stream()
                .map(PermissionAttachmentInfo::getPermission)
                .filter(perm -> perm.startsWith(permissionPrefix))
                .map(perm -> perm.substring(permissionPrefix.length()))
                .filter(suffix -> NUMBER_PATTERN.matcher(suffix).matches())
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(defaultValue);
    }

    /**
     * Generates a unique cache key for player + permission combination
     */
    private static String generateCacheKey(Player player, String permissionPrefix, String type) {
        return player.getUniqueId() + ":" + permissionPrefix + ":" + type;
    }

    /**
     * Extracts a numeric value from a permission with caching
     *
     * @param player           The player to check permissions for
     * @param permissionPrefix The permission prefix to match (e.g., "plugin.sethomes.")
     * @param defaultValue     The default value if no matching permission is found
     * @return The extracted number or default value
     */
    public static int getPermissionNumber(Player player, String permissionPrefix, int defaultValue) {
        String cacheKey = generateCacheKey(player, permissionPrefix, "number");

        Integer result = NUMBER_CACHE.get(cacheKey, key -> {
            return player.getEffectivePermissions().stream()
                    .map(PermissionAttachmentInfo::getPermission)
                    .filter(perm -> perm.startsWith(permissionPrefix))
                    .map(perm -> perm.substring(permissionPrefix.length()))
                    .filter(suffix -> NUMBER_PATTERN.matcher(suffix).matches())
                    .mapToInt(Integer::parseInt)
                    .max()
                    .orElse(defaultValue);
        });

        return result != null ? result : defaultValue;
    }

    /**
     * Extracts a string value from a permission with caching
     *
     * @param player           The player to check permissions for
     * @param permissionPrefix The permission prefix to match
     * @param defaultValue     The default value if no matching permission is found
     * @return The extracted string or default value
     */
    public static String getPermissionString(Player player, String permissionPrefix, String defaultValue) {
        String cacheKey = generateCacheKey(player, permissionPrefix, "string");

        return STRING_CACHE.get(cacheKey, key -> {
            return player.getEffectivePermissions().stream()
                    .map(PermissionAttachmentInfo::getPermission)
                    .filter(perm -> perm.startsWith(permissionPrefix))
                    .map(perm -> perm.substring(permissionPrefix.length()))
                    .findFirst()
                    .orElse(defaultValue);
        });
    }

    /**
     * Gets the highest permission value for a prefix with bounds checking and caching
     *
     * @param player           The player to check permissions for
     * @param permissionPrefix The permission prefix to match
     * @param defaultValue     The default value if no matching permission is found
     * @param minValue         Minimum allowed value
     * @param maxValue         Maximum allowed value
     * @return The extracted number or default value within bounds
     */
    public static int getPermissionNumberWithBounds(Player player, String permissionPrefix,
                                                    int defaultValue, int minValue, int maxValue) {
        String cacheKey = generateCacheKey(player, permissionPrefix, "bounded_" + minValue + "_" + maxValue);

        Integer result = NUMBER_CACHE.get(cacheKey, key -> {
            int value = getPermissionNumberUncached(player, permissionPrefix, defaultValue);
            return Math.max(minValue, Math.min(maxValue, value));
        });

        return result != null ? result : Math.max(minValue, Math.min(maxValue, defaultValue));
    }

    /**
     * Generic cached method to extract and parse permission values
     *
     * @param player           The player to check permissions for
     * @param permissionPrefix The permission prefix to match
     * @param parser           Function to parse the suffix string
     * @param cacheKeyType     Unique identifier for this parser type (for caching)
     * @return Optional containing the parsed value or empty if not found/parsing failed
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getPermissionValue(Player player, String permissionPrefix, java.util.function.Function<String, T> parser, String cacheKeyType) {
        String cacheKey = generateCacheKey(player, permissionPrefix, cacheKeyType);

        Object cached = GENERIC_CACHE.get(cacheKey, key -> {
            Optional<T> result = player.getEffectivePermissions().stream()
                    .map(PermissionAttachmentInfo::getPermission)
                    .filter(perm -> perm.startsWith(permissionPrefix))
                    .map(perm -> perm.substring(permissionPrefix.length()))
                    .map(suffix -> {
                        try {
                            return parser.apply(suffix);
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(java.util.Objects::nonNull)
                    .findFirst();

            return result.orElse(null);
        });

        return cached != null ? Optional.of((T) cached) : Optional.empty();
    }

    /**
     * Gets the highest permission value for a prefix with bounds checking and caching
     *
     * @param player           The player to check permissions for
     * @param permissionPrefix The permission prefix to match
     * @param defaultValue     The default value if no matching permission is found
     * @param minValue         Minimum allowed value
     * @param maxValue         Maximum allowed value
     * @return The highest extracted number or default value within bounds
     */
    public static int getHighestPermissionNumberWithBounds(Player player, String permissionPrefix, int defaultValue, int minValue, int maxValue) {
        String cacheKey = generateCacheKey(player, permissionPrefix, "highest_bounded_" + minValue + "_" + maxValue);

        Integer result = NUMBER_CACHE.get(cacheKey, key -> {
            int value = getHighestPermissionNumberUncached(player, permissionPrefix, defaultValue);
            return Math.max(minValue, Math.min(maxValue, value));
        });

        return result != null ? result : Math.max(minValue, Math.min(maxValue, defaultValue));
    }

    /**
     * Gets the lowest permission value for a prefix with bounds checking and caching
     *
     * @param player           The player to check permissions for
     * @param permissionPrefix The permission prefix to match
     * @param defaultValue     The default value if no matching permission is found
     * @param minValue         Minimum allowed value
     * @param maxValue         Maximum allowed value
     * @return The lowest extracted number or default value within bounds
     */
    public static int getLowestPermissionNumberWithBounds(Player player, String permissionPrefix, int defaultValue, int minValue, int maxValue) {
        String cacheKey = generateCacheKey(player, permissionPrefix, "lowest_bounded_" + minValue + "_" + maxValue);

        Integer result = NUMBER_CACHE.get(cacheKey, key -> {
            int value = getLowestPermissionNumberUncached(player, permissionPrefix, defaultValue);
            return Math.max(minValue, Math.min(maxValue, value));
        });

        return result != null ? result : Math.max(minValue, Math.min(maxValue, defaultValue));
    }

    /**
     * Uncached version for internal use to avoid cache recursion - gets highest value
     */
    private static int getHighestPermissionNumberUncached(Player player, String permissionPrefix, int defaultValue) {
        return player.getEffectivePermissions().stream()
                .map(PermissionAttachmentInfo::getPermission)
                .filter(perm -> perm.startsWith(permissionPrefix))
                .map(perm -> perm.substring(permissionPrefix.length()))
                .filter(suffix -> NUMBER_PATTERN.matcher(suffix).matches())
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(defaultValue);
    }

    /**
     * Uncached version for internal use to avoid cache recursion - gets lowest value
     */
    private static int getLowestPermissionNumberUncached(Player player, String permissionPrefix, int defaultValue) {
        return player.getEffectivePermissions().stream()
                .map(PermissionAttachmentInfo::getPermission)
                .filter(perm -> perm.startsWith(permissionPrefix))
                .map(perm -> perm.substring(permissionPrefix.length()))
                .filter(suffix -> NUMBER_PATTERN.matcher(suffix).matches())
                .mapToInt(Integer::parseInt)
                .min()
                .orElse(defaultValue);
    }

    /**
     * Invalidates all cached permissions for a specific player
     * Call this when player permissions change
     */
    public static void invalidatePlayerCache(Player player) {
        String playerPrefix = player.getUniqueId() + ":";

        // Remove all entries for this player
        NUMBER_CACHE.asMap().keySet().removeIf(key -> key.startsWith(playerPrefix));
        STRING_CACHE.asMap().keySet().removeIf(key -> key.startsWith(playerPrefix));
        GENERIC_CACHE.asMap().keySet().removeIf(key -> key.startsWith(playerPrefix));
    }

    /**
     * Invalidates cache for a specific player and permission prefix
     */
    public static void invalidatePermissionCache(Player player, String permissionPrefix) {
        String keyPrefix = player.getUniqueId() + ":" + permissionPrefix + ":";

        NUMBER_CACHE.asMap().keySet().removeIf(key -> key.startsWith(keyPrefix));
        STRING_CACHE.asMap().keySet().removeIf(key -> key.startsWith(keyPrefix));
        GENERIC_CACHE.asMap().keySet().removeIf(key -> key.startsWith(keyPrefix));
    }

    /**
     * Clears all permission caches - useful for plugin reload
     */
    public static void clearAllCaches() {
        NUMBER_CACHE.invalidateAll();
        STRING_CACHE.invalidateAll();
        GENERIC_CACHE.invalidateAll();
    }

    /**
     * Gets cache statistics for monitoring
     */
    public static String getCacheStats() {
        return String.format(
                "Permission Cache Stats:\n" +
                        "Numbers: %s\n" +
                        "Strings: %s\n" +
                        "Generic: %s",
                NUMBER_CACHE.stats(),
                STRING_CACHE.stats(),
                GENERIC_CACHE.stats()
        );
    }
}

// Maven dependency needed:
// com.github.ben-manes.caffeine:caffeine:3.2.0
// <dependency>
//   <groupId>com.github.ben-manes.caffeine</groupId>
//   <artifactId>caffeine</artifactId>
//   <version>3.1.8</version>
// </dependency>