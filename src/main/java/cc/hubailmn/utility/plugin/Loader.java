package cc.hubailmn.utility.plugin;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.apache.logging.log4j.core.config.Configurator;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

public abstract class Loader implements PluginLoader {

    protected final MavenLibraryResolver resolver = new MavenLibraryResolver();

    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        Configurator.setLevel("SpigotLibraryLoader", org.apache.logging.log4j.Level.OFF);

        addDefaultRepositories();
        addDefaultDependencies();

        addRepository();
        addLibrary();

        classpathBuilder.addLibrary(resolver);
    }

    public abstract void addLibrary();

    public abstract void addRepository();

    protected void addDefaultRepositories() {
        resolver.addRepository(new RemoteRepository.Builder(
                "maven-central", "default", "https://repo1.maven.org/maven2/"
        ).build());

        resolver.addRepository(new RemoteRepository.Builder(
                "alfresco", "default", "https://artifacts.alfresco.com/nexus/content/repositories/public/"
        ).build());

        resolver.addRepository(new RemoteRepository.Builder(
                "jitpack", "default", "https://jitpack.io"
        ).build());
    }

    protected void addDefaultDependencies() {
        addLibrary("org.openjdk.nashorn:nashorn-core:15.6");
    }

    public void addLibrary(String mavenCoordinate) {
        resolver.addDependency(new Dependency(new DefaultArtifact(mavenCoordinate), null));
    }

    public void addRepository(String id, String url) {
        resolver.addRepository(new RemoteRepository.Builder(id, "default", url).build());
    }
}
