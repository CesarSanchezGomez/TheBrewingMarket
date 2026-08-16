package com.cesarcosmico.thebrewingmarket;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

public final class TheBrewingMarketLoader implements PluginLoader {

    private static final String[] LIBRARIES = {
            "com.zaxxer:HikariCP:7.1.0",
            "org.mariadb.jdbc:mariadb-java-client:3.5.10",
            "com.mysql:mysql-connector-j:9.7.0",
            "org.xerial:sqlite-jdbc:3.53.2.1"
    };

    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addRepository(new RemoteRepository.Builder(
                "central", "default", MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR).build());
        for (String coordinates : LIBRARIES) {
            resolver.addDependency(new Dependency(new DefaultArtifact(coordinates), null));
        }
        classpathBuilder.addLibrary(resolver);
    }
}