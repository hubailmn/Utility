package cc.hubailmn.utility.registry;

import cc.hubailmn.utility.BasePlugin;
import cc.hubailmn.utility.interaction.CSend;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

public final class ClasspathScanner {

    private static final Pattern ANONYMOUS_CLASS_PATTERN = Pattern.compile(".*\\$\\d+.*");

    private ClasspathScanner() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Scan plugin JAR for classes annotated with a given annotation.
     */
    public static Set<Class<?>> getTypesAnnotatedWith(Class<?> annotation, String... packages) {
        Set<Class<?>> result = new HashSet<>();
        for (Class<?> clazz : findCandidateClasses(packages)) {
            if (clazz.isAnnotationPresent((Class) annotation)) {
                result.add(clazz);
            }
        }
        return result;
    }

    /**
     * Scan plugin JAR for subtypes of a given supertype.
     */
    public static <T> Set<Class<? extends T>> getSubTypesOf(Class<T> superType, String... packages) {
        Set<Class<? extends T>> result = new HashSet<>();
        for (Class<?> clazz : findCandidateClasses(packages)) {
            if (superType.isAssignableFrom(clazz) && !clazz.equals(superType)) {
                @SuppressWarnings("unchecked")
                Class<? extends T> typed = (Class<? extends T>) clazz;
                result.add(typed);
            }
        }
        return result;
    }

    /**
     * Internal: find non-abstract, non-anonymous classes inside plugin jar.
     */
    private static List<Class<?>> findCandidateClasses(String... packages) {
        List<Class<?>> classes = new ArrayList<>();
        File source = BasePlugin.getSource();

        try (JarFile jar = new JarFile(source)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                if (!name.endsWith(".class")) continue;

                String className = name.replace('/', '.').substring(0, name.length() - 6);

                if (!matchesPackages(className, packages)) continue;
                if (ANONYMOUS_CLASS_PATTERN.matcher(className).matches()) continue;

                try {
                    Class<?> clazz = BasePlugin.class.getClassLoader().loadClass(className);
                    if (!Modifier.isAbstract(clazz.getModifiers())) {
                        classes.add(clazz);
                    }
                } catch (ClassNotFoundException | NoClassDefFoundError | IncompatibleClassChangeError |
                         VerifyError | ClassFormatError e) {
                    // Log at debug level so you know what got skipped
                    CSend.debug("Skipped class {}: {}", className, e.getClass().getSimpleName());
                }
            }
        } catch (IOException e) {
            CSend.error("Error reading plugin jar: {}", e.getMessage());
        }

        return classes;
    }

    private static boolean matchesPackages(String className, String[] packages) {
        Set<String> effectivePackages = new LinkedHashSet<>();

        effectivePackages.add("cc.hubailmn.utility");

        if (BasePlugin.getInstance().isScanFullPackage()) {
            effectivePackages.add(BasePlugin.getInstance().getPackageName());
        } else if (packages != null) {
            Collections.addAll(effectivePackages, packages);
        }

        if (effectivePackages.isEmpty()) {
            return true;
        }

        for (String pkg : effectivePackages) {
            if (className.startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }

}
