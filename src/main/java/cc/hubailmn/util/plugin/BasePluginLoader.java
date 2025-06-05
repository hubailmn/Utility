package cc.hubailmn.util.plugin;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.apache.logging.log4j.core.config.Configurator;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

public class BasePluginLoader implements PluginLoader {

    @Override
    public void classloader(@NotNull PluginClasspathBuilder pluginClasspathBuilder) {
        Configurator.setLevel("SpigotLibraryLoader", org.apache.logging.log4j.Level.OFF);

        MavenLibraryResolver resolver = new MavenLibraryResolver();

        resolver.addRepository(
                new RemoteRepository.Builder(
                        "central",
                        "default",
                        "https://repo.maven.apache.org/maven2/"
                ).build()
        );

        resolver.addRepository(
                new RemoteRepository.Builder(
                        "jitpack.io",
                        "default",
                        "https://jitpack.io"
                ).build()
        );

        resolver.addDependency(new Dependency(new DefaultArtifact("org.reflections:reflections:0.10.2"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("com.github.hubailmn:Utility:v1.0.1"), null));

        pluginClasspathBuilder.addLibrary(resolver);
    }
}