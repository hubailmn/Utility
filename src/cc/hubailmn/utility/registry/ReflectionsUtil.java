package cc.hubailmn.utility.registry;

import cc.hubailmn.utility.BasePlugin;
import cc.hubailmn.utility.interaction.CSend;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;

public final class ReflectionsUtil {

    private ReflectionsUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    @SuppressWarnings("deprecation")
    public static Reflections build(String... packages) {
        if (BasePlugin.getInstance().isScanFullPackage()) {
            packages = new String[]{BasePlugin.getInstance().getPackageName()};
        }

        if (packages == null || packages.length == 0) {
            CSend.warn("No package paths provided for scanning.");
        }

        return new Reflections(new ConfigurationBuilder()
                .forPackages(packages)
                .addClassLoaders(BasePlugin.getInstance().getClass().getClassLoader())
                .addScanners(new TypeAnnotationsScanner(), new SubTypesScanner(false)));
    }
}
