package cc.hubailmn.util.Registry;

import cc.hubailmn.util.interaction.CSend;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class ReflectionsUtil {

    private ReflectionsUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Set<Class<?>> getClassesWithAnnotation(Class<? extends Annotation> annotation, String... packageNames) {
        Set<Class<?>> annotated = new HashSet<>();
        for (Class<?> clazz : getAllClassesInPackages(packageNames)) {
            if (clazz.isAnnotationPresent(annotation)) {
                annotated.add(clazz);
            }
        }
        return annotated;
    }

    public static <T> Set<Class<? extends T>> getSubtypesOf(Class<T> superclass, String... packageNames) {
        Set<Class<? extends T>> matched = new HashSet<>();
        for (Class<?> clazz : getAllClassesInPackages(packageNames)) {
            if (superclass.isAssignableFrom(clazz) && !clazz.equals(superclass)) {
                matched.add(clazz.asSubclass(superclass));
            }
        }
        return matched;
    }

    public static Set<Class<?>> getAllClassesInPackages(String... packageNames) {
        Set<Class<?>> classes = new HashSet<>();
        for (String pkg : packageNames) {
            classes.addAll(getAllClassesInPackage(pkg));
        }
        return classes;
    }

    public static Set<Class<?>> getAllClassesInPackage(String packageName) {
        Set<Class<?>> classes = new HashSet<>();
        String path = packageName.replace('.', '/');
        try {
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(path);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                if (url.getProtocol().equals("jar")) {
                    String jarPath = url.getPath().substring(5, url.getPath().indexOf("!"));
                    try (JarFile jarFile = new JarFile(jarPath)) {
                        for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements(); ) {
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            if (name.endsWith(".class") && name.startsWith(path)) {
                                String className = name.replace('/', '.').replace(".class", "");
                                try {
                                    classes.add(Class.forName(className));
                                } catch (Throwable ignored) {
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            CSend.error(e.getMessage());
            CSend.error(e);
        }
        return classes;
    }
}
