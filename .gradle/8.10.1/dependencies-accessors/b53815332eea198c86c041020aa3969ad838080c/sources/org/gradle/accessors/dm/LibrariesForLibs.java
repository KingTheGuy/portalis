package org.gradle.accessors.dm;

import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.MinimalExternalModuleDependency;
import org.gradle.plugin.use.PluginDependency;
import org.gradle.api.artifacts.ExternalModuleDependencyBundle;
import org.gradle.api.artifacts.MutableVersionConstraint;
import org.gradle.api.provider.Provider;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.internal.catalog.AbstractExternalDependencyFactory;
import org.gradle.api.internal.catalog.DefaultVersionCatalog;
import java.util.Map;
import org.gradle.api.internal.attributes.ImmutableAttributesFactory;
import org.gradle.api.internal.artifacts.dsl.CapabilityNotationParser;
import javax.inject.Inject;

/**
 * A catalog of dependencies accessible via the {@code libs} extension.
 */
@NonNullApi
public class LibrariesForLibs extends AbstractExternalDependencyFactory {

    private final AbstractExternalDependencyFactory owner = this;
    private final ComLibraryAccessors laccForComLibraryAccessors = new ComLibraryAccessors(owner);
    private final IoLibraryAccessors laccForIoLibraryAccessors = new IoLibraryAccessors(owner);
    private final OrgLibraryAccessors laccForOrgLibraryAccessors = new OrgLibraryAccessors(owner);
    private final VersionAccessors vaccForVersionAccessors = new VersionAccessors(providers, config);
    private final BundleAccessors baccForBundleAccessors = new BundleAccessors(objects, providers, config, attributesFactory, capabilityNotationParser);
    private final PluginAccessors paccForPluginAccessors = new PluginAccessors(providers, config);

    @Inject
    public LibrariesForLibs(DefaultVersionCatalog config, ProviderFactory providers, ObjectFactory objects, ImmutableAttributesFactory attributesFactory, CapabilityNotationParser capabilityNotationParser) {
        super(config, providers, objects, attributesFactory, capabilityNotationParser);
    }

    /**
     * Group of libraries at <b>com</b>
     */
    public ComLibraryAccessors getCom() {
        return laccForComLibraryAccessors;
    }

    /**
     * Group of libraries at <b>io</b>
     */
    public IoLibraryAccessors getIo() {
        return laccForIoLibraryAccessors;
    }

    /**
     * Group of libraries at <b>org</b>
     */
    public OrgLibraryAccessors getOrg() {
        return laccForOrgLibraryAccessors;
    }

    /**
     * Group of versions at <b>versions</b>
     */
    public VersionAccessors getVersions() {
        return vaccForVersionAccessors;
    }

    /**
     * Group of bundles at <b>bundles</b>
     */
    public BundleAccessors getBundles() {
        return baccForBundleAccessors;
    }

    /**
     * Group of plugins at <b>plugins</b>
     */
    public PluginAccessors getPlugins() {
        return paccForPluginAccessors;
    }

    public static class ComLibraryAccessors extends SubDependencyFactory {
        private final ComMoandjiezanaLibraryAccessors laccForComMoandjiezanaLibraryAccessors = new ComMoandjiezanaLibraryAccessors(owner);

        public ComLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>com.moandjiezana</b>
         */
        public ComMoandjiezanaLibraryAccessors getMoandjiezana() {
            return laccForComMoandjiezanaLibraryAccessors;
        }

    }

    public static class ComMoandjiezanaLibraryAccessors extends SubDependencyFactory {
        private final ComMoandjiezanaTomlLibraryAccessors laccForComMoandjiezanaTomlLibraryAccessors = new ComMoandjiezanaTomlLibraryAccessors(owner);

        public ComMoandjiezanaLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>com.moandjiezana.toml</b>
         */
        public ComMoandjiezanaTomlLibraryAccessors getToml() {
            return laccForComMoandjiezanaTomlLibraryAccessors;
        }

    }

    public static class ComMoandjiezanaTomlLibraryAccessors extends SubDependencyFactory {

        public ComMoandjiezanaTomlLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>toml4j</b> with <b>com.moandjiezana.toml:toml4j</b> coordinates and
         * with version reference <b>com.moandjiezana.toml.toml4j</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getToml4j() {
            return create("com.moandjiezana.toml.toml4j");
        }

    }

    public static class IoLibraryAccessors extends SubDependencyFactory {
        private final IoPapermcLibraryAccessors laccForIoPapermcLibraryAccessors = new IoPapermcLibraryAccessors(owner);

        public IoLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>io.papermc</b>
         */
        public IoPapermcLibraryAccessors getPapermc() {
            return laccForIoPapermcLibraryAccessors;
        }

    }

    public static class IoPapermcLibraryAccessors extends SubDependencyFactory {
        private final IoPapermcPaperLibraryAccessors laccForIoPapermcPaperLibraryAccessors = new IoPapermcPaperLibraryAccessors(owner);

        public IoPapermcLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>io.papermc.paper</b>
         */
        public IoPapermcPaperLibraryAccessors getPaper() {
            return laccForIoPapermcPaperLibraryAccessors;
        }

    }

    public static class IoPapermcPaperLibraryAccessors extends SubDependencyFactory {
        private final IoPapermcPaperPaperLibraryAccessors laccForIoPapermcPaperPaperLibraryAccessors = new IoPapermcPaperPaperLibraryAccessors(owner);

        public IoPapermcPaperLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>io.papermc.paper.paper</b>
         */
        public IoPapermcPaperPaperLibraryAccessors getPaper() {
            return laccForIoPapermcPaperPaperLibraryAccessors;
        }

    }

    public static class IoPapermcPaperPaperLibraryAccessors extends SubDependencyFactory {

        public IoPapermcPaperPaperLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>api</b> with <b>io.papermc.paper:paper-api</b> coordinates and
         * with version reference <b>io.papermc.paper.paper.api</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getApi() {
            return create("io.papermc.paper.paper.api");
        }

    }

    public static class OrgLibraryAccessors extends SubDependencyFactory {
        private final OrgApacheLibraryAccessors laccForOrgApacheLibraryAccessors = new OrgApacheLibraryAccessors(owner);

        public OrgLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>org.apache</b>
         */
        public OrgApacheLibraryAccessors getApache() {
            return laccForOrgApacheLibraryAccessors;
        }

    }

    public static class OrgApacheLibraryAccessors extends SubDependencyFactory {
        private final OrgApacheMavenLibraryAccessors laccForOrgApacheMavenLibraryAccessors = new OrgApacheMavenLibraryAccessors(owner);

        public OrgApacheLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>org.apache.maven</b>
         */
        public OrgApacheMavenLibraryAccessors getMaven() {
            return laccForOrgApacheMavenLibraryAccessors;
        }

    }

    public static class OrgApacheMavenLibraryAccessors extends SubDependencyFactory {
        private final OrgApacheMavenPluginsLibraryAccessors laccForOrgApacheMavenPluginsLibraryAccessors = new OrgApacheMavenPluginsLibraryAccessors(owner);

        public OrgApacheMavenLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>org.apache.maven.plugins</b>
         */
        public OrgApacheMavenPluginsLibraryAccessors getPlugins() {
            return laccForOrgApacheMavenPluginsLibraryAccessors;
        }

    }

    public static class OrgApacheMavenPluginsLibraryAccessors extends SubDependencyFactory {
        private final OrgApacheMavenPluginsMavenLibraryAccessors laccForOrgApacheMavenPluginsMavenLibraryAccessors = new OrgApacheMavenPluginsMavenLibraryAccessors(owner);

        public OrgApacheMavenPluginsLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>org.apache.maven.plugins.maven</b>
         */
        public OrgApacheMavenPluginsMavenLibraryAccessors getMaven() {
            return laccForOrgApacheMavenPluginsMavenLibraryAccessors;
        }

    }

    public static class OrgApacheMavenPluginsMavenLibraryAccessors extends SubDependencyFactory {
        private final OrgApacheMavenPluginsMavenAssemblyLibraryAccessors laccForOrgApacheMavenPluginsMavenAssemblyLibraryAccessors = new OrgApacheMavenPluginsMavenAssemblyLibraryAccessors(owner);

        public OrgApacheMavenPluginsMavenLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>org.apache.maven.plugins.maven.assembly</b>
         */
        public OrgApacheMavenPluginsMavenAssemblyLibraryAccessors getAssembly() {
            return laccForOrgApacheMavenPluginsMavenAssemblyLibraryAccessors;
        }

    }

    public static class OrgApacheMavenPluginsMavenAssemblyLibraryAccessors extends SubDependencyFactory {

        public OrgApacheMavenPluginsMavenAssemblyLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>plugin</b> with <b>org.apache.maven.plugins:maven-assembly-plugin</b> coordinates and
         * with version reference <b>org.apache.maven.plugins.maven.assembly.plugin</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getPlugin() {
            return create("org.apache.maven.plugins.maven.assembly.plugin");
        }

    }

    public static class VersionAccessors extends VersionFactory  {

        private final ComVersionAccessors vaccForComVersionAccessors = new ComVersionAccessors(providers, config);
        private final IoVersionAccessors vaccForIoVersionAccessors = new IoVersionAccessors(providers, config);
        private final OrgVersionAccessors vaccForOrgVersionAccessors = new OrgVersionAccessors(providers, config);
        public VersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.com</b>
         */
        public ComVersionAccessors getCom() {
            return vaccForComVersionAccessors;
        }

        /**
         * Group of versions at <b>versions.io</b>
         */
        public IoVersionAccessors getIo() {
            return vaccForIoVersionAccessors;
        }

        /**
         * Group of versions at <b>versions.org</b>
         */
        public OrgVersionAccessors getOrg() {
            return vaccForOrgVersionAccessors;
        }

    }

    public static class ComVersionAccessors extends VersionFactory  {

        private final ComMoandjiezanaVersionAccessors vaccForComMoandjiezanaVersionAccessors = new ComMoandjiezanaVersionAccessors(providers, config);
        public ComVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.com.moandjiezana</b>
         */
        public ComMoandjiezanaVersionAccessors getMoandjiezana() {
            return vaccForComMoandjiezanaVersionAccessors;
        }

    }

    public static class ComMoandjiezanaVersionAccessors extends VersionFactory  {

        private final ComMoandjiezanaTomlVersionAccessors vaccForComMoandjiezanaTomlVersionAccessors = new ComMoandjiezanaTomlVersionAccessors(providers, config);
        public ComMoandjiezanaVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.com.moandjiezana.toml</b>
         */
        public ComMoandjiezanaTomlVersionAccessors getToml() {
            return vaccForComMoandjiezanaTomlVersionAccessors;
        }

    }

    public static class ComMoandjiezanaTomlVersionAccessors extends VersionFactory  {

        public ComMoandjiezanaTomlVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Version alias <b>com.moandjiezana.toml.toml4j</b> with value <b>0.7.2</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getToml4j() { return getVersion("com.moandjiezana.toml.toml4j"); }

    }

    public static class IoVersionAccessors extends VersionFactory  {

        private final IoPapermcVersionAccessors vaccForIoPapermcVersionAccessors = new IoPapermcVersionAccessors(providers, config);
        public IoVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.io.papermc</b>
         */
        public IoPapermcVersionAccessors getPapermc() {
            return vaccForIoPapermcVersionAccessors;
        }

    }

    public static class IoPapermcVersionAccessors extends VersionFactory  {

        private final IoPapermcPaperVersionAccessors vaccForIoPapermcPaperVersionAccessors = new IoPapermcPaperVersionAccessors(providers, config);
        public IoPapermcVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.io.papermc.paper</b>
         */
        public IoPapermcPaperVersionAccessors getPaper() {
            return vaccForIoPapermcPaperVersionAccessors;
        }

    }

    public static class IoPapermcPaperVersionAccessors extends VersionFactory  {

        private final IoPapermcPaperPaperVersionAccessors vaccForIoPapermcPaperPaperVersionAccessors = new IoPapermcPaperPaperVersionAccessors(providers, config);
        public IoPapermcPaperVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.io.papermc.paper.paper</b>
         */
        public IoPapermcPaperPaperVersionAccessors getPaper() {
            return vaccForIoPapermcPaperPaperVersionAccessors;
        }

    }

    public static class IoPapermcPaperPaperVersionAccessors extends VersionFactory  {

        public IoPapermcPaperPaperVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Version alias <b>io.papermc.paper.paper.api</b> with value <b>1.20.4-R0.1-SNAPSHOT</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getApi() { return getVersion("io.papermc.paper.paper.api"); }

    }

    public static class OrgVersionAccessors extends VersionFactory  {

        private final OrgApacheVersionAccessors vaccForOrgApacheVersionAccessors = new OrgApacheVersionAccessors(providers, config);
        public OrgVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.org.apache</b>
         */
        public OrgApacheVersionAccessors getApache() {
            return vaccForOrgApacheVersionAccessors;
        }

    }

    public static class OrgApacheVersionAccessors extends VersionFactory  {

        private final OrgApacheMavenVersionAccessors vaccForOrgApacheMavenVersionAccessors = new OrgApacheMavenVersionAccessors(providers, config);
        public OrgApacheVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.org.apache.maven</b>
         */
        public OrgApacheMavenVersionAccessors getMaven() {
            return vaccForOrgApacheMavenVersionAccessors;
        }

    }

    public static class OrgApacheMavenVersionAccessors extends VersionFactory  {

        private final OrgApacheMavenPluginsVersionAccessors vaccForOrgApacheMavenPluginsVersionAccessors = new OrgApacheMavenPluginsVersionAccessors(providers, config);
        public OrgApacheMavenVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.org.apache.maven.plugins</b>
         */
        public OrgApacheMavenPluginsVersionAccessors getPlugins() {
            return vaccForOrgApacheMavenPluginsVersionAccessors;
        }

    }

    public static class OrgApacheMavenPluginsVersionAccessors extends VersionFactory  {

        private final OrgApacheMavenPluginsMavenVersionAccessors vaccForOrgApacheMavenPluginsMavenVersionAccessors = new OrgApacheMavenPluginsMavenVersionAccessors(providers, config);
        public OrgApacheMavenPluginsVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.org.apache.maven.plugins.maven</b>
         */
        public OrgApacheMavenPluginsMavenVersionAccessors getMaven() {
            return vaccForOrgApacheMavenPluginsMavenVersionAccessors;
        }

    }

    public static class OrgApacheMavenPluginsMavenVersionAccessors extends VersionFactory  {

        private final OrgApacheMavenPluginsMavenAssemblyVersionAccessors vaccForOrgApacheMavenPluginsMavenAssemblyVersionAccessors = new OrgApacheMavenPluginsMavenAssemblyVersionAccessors(providers, config);
        public OrgApacheMavenPluginsMavenVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.org.apache.maven.plugins.maven.assembly</b>
         */
        public OrgApacheMavenPluginsMavenAssemblyVersionAccessors getAssembly() {
            return vaccForOrgApacheMavenPluginsMavenAssemblyVersionAccessors;
        }

    }

    public static class OrgApacheMavenPluginsMavenAssemblyVersionAccessors extends VersionFactory  {

        public OrgApacheMavenPluginsMavenAssemblyVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Version alias <b>org.apache.maven.plugins.maven.assembly.plugin</b> with value <b>3.6.0</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getPlugin() { return getVersion("org.apache.maven.plugins.maven.assembly.plugin"); }

    }

    public static class BundleAccessors extends BundleFactory {

        public BundleAccessors(ObjectFactory objects, ProviderFactory providers, DefaultVersionCatalog config, ImmutableAttributesFactory attributesFactory, CapabilityNotationParser capabilityNotationParser) { super(objects, providers, config, attributesFactory, capabilityNotationParser); }

    }

    public static class PluginAccessors extends PluginFactory {

        public PluginAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

    }

}
