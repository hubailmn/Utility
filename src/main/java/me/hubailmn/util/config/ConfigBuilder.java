package me.hubailmn.util.config;

import lombok.Data;
import me.hubailmn.util.BasePlugin;
import me.hubailmn.util.annotation.LoadConfig;
import me.hubailmn.util.interaction.CSend;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Data
public class ConfigBuilder {

    private final String fileName;
    private final File file;

    private FileConfiguration config;

    public ConfigBuilder() {
        LoadConfig annotation = this.getClass().getAnnotation(LoadConfig.class);

        this.fileName = annotation.path();

        CSend.debug("Loaded config: " + fileName);

        this.file = new File(BasePlugin.getInstance().getDataFolder(), fileName);
        createAndLoad();
    }

    private void createAndLoad() {
        if (!file.exists()) {
            BasePlugin.getInstance().saveResource(fileName, false);
        }

        config = YamlConfiguration.loadConfiguration(file);

        InputStream resource = BasePlugin.getInstance().getResource(fileName);
        if (resource != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(resource));
            config.setDefaults(defaultConfig);
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
    }
}
