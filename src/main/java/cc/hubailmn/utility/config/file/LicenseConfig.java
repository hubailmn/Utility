package cc.hubailmn.utility.config.file;

import cc.hubailmn.utility.annotation.IgnoreFile;
import cc.hubailmn.utility.config.ConfigBuilder;
import cc.hubailmn.utility.config.annotation.LoadConfig;
import lombok.Getter;

@LoadConfig(path = "license/License.yml")
@IgnoreFile(license = true)
@Getter
public class LicenseConfig extends ConfigBuilder {

    private String key;

    public LicenseConfig() {
        super();
        load();
    }

    public void load() {
        key = getConfig().getString("license.key");
    }

}
