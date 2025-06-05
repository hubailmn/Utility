package cc.hubailmn.util.config.file;

import cc.hubailmn.util.annotation.IgnoreFile;
import cc.hubailmn.util.config.ConfigBuilder;
import cc.hubailmn.util.config.annotation.LoadConfig;

@LoadConfig(path = "License.yml")
@IgnoreFile(license = true)
public class LicenseConfig extends ConfigBuilder {

    public String getKey() {
        return getConfig().getString("license.key");
    }

}
