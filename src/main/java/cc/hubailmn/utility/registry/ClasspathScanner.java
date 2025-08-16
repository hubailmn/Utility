package cc.hubailmn.utility.registry;

import cc.hubailmn.utility.BasePlugin;
import cc.hubailmn.utility.interaction.CSend;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

public final class ClasspathScanner {

    private static final Pattern ANONYMOUS_CLASS_PATTERN = Pattern.compile(".*\\$\\d+.*");
    private static final String CLASS_EXTENSION = ".class";
    private static final int CLASS_EXTENSION_LENGTH = CLASS_EXTENSION.length();

    private static volatile List<Class<?>> cachedCandidateClasses;
    private static volatile String[] lastPackages;

    private ClasspathScanner() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Scan plugin JAR for classes annotated with a given annotation.
     */
    public static Set<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> annotation, String... packages) {
        Set<Class<?>> result = new HashSet<>();
        for (Class<?> clazz : findCandidateClasses(packages)) {
            if (clazz.isAnnotationPresent(annotation)) {
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
     * Uses simple caching to avoid repeated JAR scanning when packages haven't changed.
     */
    private static List<Class<?>> findCandidateClasses(String... packages) {
        if (cachedCandidateClasses != null && Arrays.equals(packages, lastPackages)) {
            return cachedCandidateClasses;
        }

        List<Class<?>> classes = new ArrayList<>();
        File source = BasePlugin.getSource();

        try (JarFile jar = new JarFile(source)) {
            Set<String> effectivePackages = buildEffectivePackages(packages);

            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                if (!name.endsWith(CLASS_EXTENSION)) continue;

                String className = convertToClassName(name);

                if (!matchesPackages(className, effectivePackages)) continue;
                if (ANONYMOUS_CLASS_PATTERN.matcher(className).matches()) continue;

                Class<?> clazz = loadClassSafely(className);
                if (clazz != null && !Modifier.isAbstract(clazz.getModifiers())) {
                    classes.add(clazz);
                }
            }
        } catch (IOException e) {
            CSend.error("Error reading plugin jar: {}", e.getMessage());
            return Collections.emptyList();
        }

        cachedCandidateClasses = classes;
        lastPackages = packages != null ? packages.clone() : null;

        return classes;
    }

    private static Set<String> buildEffectivePackages(String[] packages) {
        Set<String> effectivePackages = new LinkedHashSet<>();

        effectivePackages.add("cc.hubailmn.utility");

        if (BasePlugin.getInstance().isScanFullPackage()) {
            effectivePackages.add(BasePlugin.getInstance().getPackageName());
        } else if (packages != null && packages.length > 0) {
            Collections.addAll(effectivePackages, packages);
        }

        return effectivePackages;
    }

    private static String convertToClassName(String jarEntryName) {
        return jarEntryName.replace('/', '.').substring(0, jarEntryName.length() - CLASS_EXTENSION_LENGTH);
    }

    private static Class<?> loadClassSafely(String className) {
        try {
            return BasePlugin.class.getClassLoader().loadClass(className);
        } catch (ClassNotFoundException | NoClassDefFoundError | IncompatibleClassChangeError | VerifyError | ClassFormatError e) {
            CSend.debug("Skipped class {}: {}", className, e.getClass().getSimpleName());
            return null;
        }
    }

    private static boolean matchesPackages(String className, Set<String> effectivePackages) {
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

    public static void clearCache() {
        cachedCandidateClasses = null;
        lastPackages = null;
    }
}