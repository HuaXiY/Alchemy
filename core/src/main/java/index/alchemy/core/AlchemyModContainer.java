package index.alchemy.core;

import java.io.File;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import index.alchemy.api.annotation.Patch;
import index.alchemy.core.debug.AlchemyRuntimeException;

import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.asm.transformers.BlamingTransformer;
import net.minecraftforge.fml.common.discovery.ModCandidate;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLEvent;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.toposort.ModSorter;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;
import net.minecraftforge.fml.common.versioning.VersionParser;
import net.minecraftforge.fml.common.versioning.VersionRange;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import static index.alchemy.core.AlchemyConstants.*;

public class AlchemyModContainer implements ModContainer {
    
    @Patch("net.minecraftforge.fml.common.toposort.ModSorter")
    public static class Patch$ModSorter extends ModSorter {
        
        @Patch.Exception
        public Patch$ModSorter(List<ModContainer> modList, Map<String, ModContainer> nameLookup) {
            super(modList, nameLookup);
        }
        
        @Override
        public List<ModContainer> sort() {
            List<ModContainer> result = sort(), alc = result.stream().filter(AlchemyModContainer.class::isInstance).collect(Collectors.toList());
            result.removeAll(alc);
            result.addAll(alc);
            return result;
        }
        
    }
    
    private File source;
    private ModMetadata modMetadata;
    private Map<String, Object> descriptor;
    private boolean enabled = true;
    private LoadController controller;
    private DefaultArtifactVersion processedVersion;
    
    private VersionRange minecraftAccepted;
    private Certificate certificate;
    private Map<String, String> customModProperties;
    private ModCandidate candidate;
    private int classVersion;
    
    public static AlchemyModContainer instance;
    
    public AlchemyModContainer(String className, ModCandidate container, Map<String, Object> modDescriptor) {
        if (instance != null)
            throw AlchemyRuntimeException.onException(new RuntimeException("instance != null"));
        instance = this;
        source = container.getModContainer();
        candidate = container;
        descriptor = modDescriptor;
        descriptor.put("name", getName());
        descriptor.put("version", getVersion());
    }
    
    @Override
    public String getModId() { return MOD_ID; }
    
    @Override
    public String getName() { return MOD_NAME; }
    
    @Override
    public String getVersion() { return MOD_VERSION; }
    
    @Override
    public File getSource() { return source; }
    
    @Override
    public ModMetadata getMetadata() { return modMetadata; }
    
    @Override
    public void bindMetadata(MetadataCollection mc) {
        modMetadata = mc.getMetadataForId(getModId(), descriptor);
        Set<ArtifactVersion> requirements = Sets.newHashSet();
        List<ArtifactVersion> dependencies = Lists.newArrayList();
        List<ArtifactVersion> dependants = Lists.newArrayList();
        modMetadata.requiredMods = requirements;
        modMetadata.dependencies = dependencies;
        modMetadata.dependants = dependants;
        minecraftAccepted = VersionParser.parseRange(MC_VERSION);
    }
    
    @Override
    public void setEnabledState(boolean enabled) { this.enabled = enabled; }
    
    @Override
    public Set<ArtifactVersion> getRequirements() { return modMetadata.requiredMods; }
    
    @Override
    public List<ArtifactVersion> getDependencies() { return modMetadata.dependencies; }
    
    @Override
    public List<ArtifactVersion> getDependants() { return modMetadata.dependants; }
    
    @Override
    public String getSortingRules() { return ""; }
    
    @Override
    public boolean matches(Object mod) { return mod == getMod(); }
    
    @Override
    public Object getMod() { return AlchemyModLoader.INSTANCE; }
    
    @Override
    public boolean registerBus(EventBus eventBus, LoadController controller) {
        if (enabled) {
            this.controller = controller;
            eventBus.register(this);
            return true;
        }
        else
            return false;
    }
    
    @Subscribe
    public void constructMod(FMLConstructionEvent event) {
        try {
            BlamingTransformer.addClasses(getModId(), candidate.getClassList());
            ModClassLoader modClassLoader = event.getModClassLoader();
            modClassLoader.addFile(source);
            modClassLoader.clearNegativeCacheFor(candidate.getClassList());
            AlchemyModLoader.tryBootstrap();
            NetworkRegistry.INSTANCE.register(this, getMod().getClass(), null, event.getASMHarvestedData());
        } catch (Throwable e) { controller.errorOccurred(this, e); }
    }
    
    @Subscribe
    public void handleFMLModStateEvent(FMLStateEvent event) {
        AlchemyModLoader.init(event.getModState());
    }
    
    @Subscribe
    public void handleFMLEvent(FMLEvent event) {
        AlchemyModLoader.onFMLEvent(event);
    }
    
    @Override
    public ArtifactVersion getProcessedVersion() {
        return processedVersion == null ? processedVersion = new DefaultArtifactVersion(getModId(), getVersion()) : processedVersion;
    }
    
    @Override
    public boolean isImmutable() { return false; }
    
    @Override
    public String getDisplayVersion() { return modMetadata.version; }
    
    @Override
    public VersionRange acceptableMinecraftVersionRange() { return minecraftAccepted; }
    
    @Override
    public Certificate getSigningCertificate() { return certificate; }
    
    @Override
    public String toString() {
        return "Alchemy:" + getModId() + "{" + getVersion() + "}";
    }
    
    @Override
    public Map<String, String> getCustomModProperties() { return customModProperties; }
    
    @Override
    public Class<?> getCustomResourcePackClass() {
        try {
            return getSource().isDirectory() ? Class.forName("net.minecraftforge.fml.client.FMLFolderResourcePack", true, getClass().getClassLoader()) :
                    Class.forName("net.minecraftforge.fml.client.FMLFileResourcePack", true, getClass().getClassLoader());
        } catch (ClassNotFoundException e) { return null; }
    }
    
    @Override
    public Map<String, String> getSharedModDescriptor() {
        Map<String, String> descriptor = Maps.newHashMap();
        descriptor.put("modsystem", "FML");
        descriptor.put("id", getModId());
        descriptor.put("version", getDisplayVersion());
        descriptor.put("name", getName());
        descriptor.put("url", modMetadata.url);
        descriptor.put("authors", modMetadata.getAuthorList());
        descriptor.put("description", modMetadata.description);
        return descriptor;
    }
    
    @Override
    public Disableable canBeDisabled() { return Disableable.NEVER; }
    
    @Override
    public String getGuiClassName() { return null; }
    
    @Override
    public List<String> getOwnedPackages() { return candidate.getContainedPackages(); }
    
    @Override
    public boolean shouldLoadInEnvironment() { return true; }
    
    @Override
    public URL getUpdateUrl() { return null; }
    
    @Override
    public void setClassVersion(int classVersion) { }
    
    @Override
    public int getClassVersion() { return classVersion; }
    
}