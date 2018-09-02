package index.alchemy.core.asm.transformer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

import net.minecraft.launchwrapper.IClassTransformer;

import org.apache.commons.io.IOUtils;

public class TransformerReplace implements IClassTransformer {

    protected static final ClassLoader RESOURCES_LOADER =
            new URLClassLoader(new URL[]{TransformerReplace.class.getProtectionDomain().getCodeSource().getLocation()});

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.startsWith("net.minecraft.") || transformedName.startsWith("net.minecraftforge.")) {
            InputStream input = RESOURCES_LOADER.getResourceAsStream("/" + transformedName.replace('.', '/') + ".class");
            try {
                if (input != null) {
                    byte result[] = IOUtils.toByteArray(input);
                    AlchemyTransformerManager.transform("<replace>" + name + "|" + transformedName);
                    return result;
                }
            } catch (IOException e) { e.printStackTrace(); }
        }
        return basicClass;
    }

}
