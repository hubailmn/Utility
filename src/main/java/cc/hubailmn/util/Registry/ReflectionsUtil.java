package cc.hubailmn.util.Registry;

import cc.hubailmn.util.BasePlugin;
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
        return new Reflections(new ConfigurationBuilder()
                .forPackages(packages)
                .addClassLoaders(BasePlugin.getInstance().getClass().getClassLoader())
                .addScanners(new TypeAnnotationsScanner(), new SubTypesScanner(false)));
    }
}
