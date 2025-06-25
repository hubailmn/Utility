package cc.hubailmn.utility.plugin;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.apache.logging.log4j.core.config.Configurator;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

public class Loader implements PluginLoader {

    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        Configurator.setLevel("SpigotLibraryLoader", org.apache.logging.log4j.Level.OFF);
        MavenLibraryResolver resolver = new MavenLibraryResolver();

        resolver.addRepository(new RemoteRepository.Builder(
                "maven-central",
                "default",
                "https://repo1.maven.org/maven2/"
        ).build());

        resolver.addRepository(new RemoteRepository.Builder(
                "alfresco",
                "default",
                "https://artifacts.alfresco.com/nexus/content/repositories/public/"
        ).build());

        resolver.addRepository(new RemoteRepository.Builder(
                "jitpack",
                "default",
                "https://jitpack.io"
        ).build());

        resolver.addDependency(new Dependency(new DefaultArtifact("org.openjdk.nashorn:nashorn-core:15.6"), null));

        classpathBuilder.addLibrary(resolver);
    }
}
