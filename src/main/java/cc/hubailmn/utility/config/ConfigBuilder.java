package cc.hubailmn.utility.config;

import cc.hubailmn.utility.BasePlugin;
import cc.hubailmn.utility.config.annotation.LoadConfig;
import cc.hubailmn.utility.plugin.CSend;
import cc.hubailmn.utility.util.TextParserUtil;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

@Data
public abstract class ConfigBuilder {

    private final String fileName;
    private final File file;

    private FileConfiguration config;

    public ConfigBuilder() {
        LoadConfig annotation = this.getClass().getAnnotation(LoadConfig.class);

        if (annotation == null) throw new RuntimeException();

        this.fileName = annotation.path();
        CSend.debug("Loaded config: " + fileName);

        this.file = new File(BasePlugin.getInstance().getDataFolder(), fileName);
        createAndLoad();
        reloadCache();
    }

    private void createAndLoad() {
        if (!file.exists()) {
            BasePlugin.getInstance().saveResource(fileName, false);
        }

        config = YamlConfiguration.loadConfiguration(file);

        try (InputStream resource = BasePlugin.getInstance().getResource(fileName)) {
            if (resource != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(resource));
                config.setDefaults(defaultConfig);
            }
        } catch (IOException ignored) {
        }

    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            CSend.error("Could not save config file: " + fileName);
            throw new RuntimeException(e);
        }
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(file);
        reloadCache();
    }

    public abstract void reloadCache();

    public Component getComponent(String path, Component def) {
        String raw = config.getString(path);
        return raw != null ? TextParserUtil.parse(raw) : def;
    }

    public String getString(Map<?, ?> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    public Component getComponent(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val instanceof String ? TextParserUtil.parse((String) val) : null;
    }

    public List<String> getStringListOrDefault(Map<?, ?> map, String key) {
        Object value = map.get(key);
        if (value instanceof List<?>) {
            return ((List<?>) value).stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public List<Map<String, Object>> getMapList(String path) {
        List<?> raw = config.getList(path);
        if (raw == null) return Collections.emptyList();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : raw) {
            if (item instanceof Map<?, ?> map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> casted = (Map<String, Object>) map;
                result.add(casted);
            }
        }
        return result;
    }

    public boolean getBool(Map<String, Object> map, String key, boolean def) {
        Object val = map.get(key);
        return val instanceof Boolean ? (Boolean) val : def;
    }

    public int getInt(Map<String, Object> map, String key, int def) {
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        try {
            return val != null ? Integer.parseInt(val.toString()) : def;
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public float getFloat(Map<String, Object> map, String key, float def) {
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).floatValue();
        try {
            return val != null ? Float.parseFloat(val.toString()) : def;
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public <E extends Enum<E>> E getEnum(Class<E> enumClass, Map<String, Object> map, String key, E def) {
        String val = String.valueOf(map.get(key));
        if (val == null) return def;
        try {
            return Enum.valueOf(enumClass, val.toUpperCase());
        } catch (IllegalArgumentException e) {
            return def;
        }
    }

    public <E extends Enum<E>> Set<E> getEnumSet(Class<E> enumClass, Map<String, Object> map, String key) {
        Object raw = map.get(key);
        if (!(raw instanceof List<?> list)) return EnumSet.noneOf(enumClass);

        EnumSet<E> set = EnumSet.noneOf(enumClass);
        for (Object obj : list) {
            if (obj == null) continue;
            try {
                E val = Enum.valueOf(enumClass, obj.toString().toUpperCase().replace('-', '_'));
                set.add(val);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return set;
    }

    public Sound getSound(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return null;
        try {
            return Sound.valueOf(val.toString().toUpperCase());
        } catch (IllegalArgumentException e) {
            CSend.warn("Unknown sound: " + val);
            return null;
        }
    }

}
