package me.hubailmn.util.config.file;

import me.hubailmn.util.annotation.IgnoreFile;
import me.hubailmn.util.config.ConfigBuilder;
import me.hubailmn.util.config.annotation.LoadConfig;

@LoadConfig(path = "License.yml")
@IgnoreFile(license = true)
public class LicenseConfig extends ConfigBuilder {

    public String getKey() {
        return getConfig().getString("license.key");
    }

}
