package index.alchemy.core.asm.transformer;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import index.alchemy.api.annotation.Hook;
import index.project.version.annotation.Dev;
import index.project.version.annotation.Omega;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import org.apache.commons.io.IOUtils;

import static index.alchemy.util.$.$;

@Dev
@Omega
@Hook.Provider
@SideOnly(Side.CLIENT)
public class TransformerInjectOptifine implements IClassTransformer {
    
    protected static final String OPTIFINE_INFO = "[OptiFine] [INFO] (Reflector) ";
    
    protected final JarFile jar;
    
    public static void tryInject(LaunchClassLoader classLoader) {
        try {
            List<IClassTransformer> transformers = $(classLoader, "transformers");
            transformers.removeIf(t -> t.getClass().getName().startsWith("optifine."));
            transformers.add(0, new TransformerInjectOptifine(classLoader));
        } catch (Exception e) { AlchemyTransformerManager.logger.debug(e); }
    }
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.startsWith("net.minecraft.world.chunk.") || transformedName.startsWith("net.minecraft.server.integrated."))
            return basicClass;
        if (!name.startsWith("net.minecraftforge.")) {
            ZipEntry entry = jar.getEntry(name.replace(".", "/") + ".class");
            if (entry != null)
                try {
                    return IOUtils.toByteArray(jar.getInputStream(entry));
                } catch (IOException e) { e.printStackTrace(); }
        }
        return basicClass;
    }
    
    protected TransformerInjectOptifine(ClassLoader classloader) throws IOException, URISyntaxException {
        ClassPath path = ClassPath.from(classloader);
        JarFile target = null;
        for (ClassInfo info : path.getTopLevelClasses("optifine"))
            if (info.getSimpleName().equals("OptiFineClassTransformer")) {
                Class<?> optifine = info.load();
                URL url = optifine.getProtectionDomain().getCodeSource().getLocation();
                if (url.getFile().endsWith(".jar"))
                    target = new JarFile(new File(url.toURI()));
            }
        if (target == null)
            throw new RuntimeException("Can't find Optifine jar file !");
        jar = target;
    }
    
}
