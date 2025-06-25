package cc.hubailmn.utility.config.file;

import cc.hubailmn.utility.annotation.IgnoreFile;
import cc.hubailmn.utility.config.ConfigBuilder;
import cc.hubailmn.utility.config.annotation.LoadConfig;

@LoadConfig(path = "license/License.yml")
@IgnoreFile(license = true)
public class LicenseConfig extends ConfigBuilder {

    public String getKey() {
        return getConfig().getString("license.key");
    }

}
