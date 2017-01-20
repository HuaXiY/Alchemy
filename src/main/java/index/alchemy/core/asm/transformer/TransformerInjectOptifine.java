package index.alchemy.core.asm.transformer;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Joiner;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import index.project.version.annotation.Dev;
import index.project.version.annotation.Omega;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;

import static index.alchemy.util.Tool.$;

@Dev
@Omega
public class TransformerInjectOptifine implements IClassTransformer {
	
	protected JarFile jar;
	
	protected TransformerInjectOptifine(ClassLoader classloader) throws IOException, URISyntaxException {
		ClassPath path = ClassPath.from(classloader);
		for (ClassInfo info : path.getTopLevelClasses("optifine"))
			if (info.getSimpleName().equals("OptiFineClassTransformer")) {
				Class<?> optifine = info.load();
				URL url = optifine.getProtectionDomain().getCodeSource().getLocation();
				if (url.getFile().endsWith(".jar"))
					jar = new JarFile(new File(url.toURI()));
			}
		if (jar == null)
			throw new RuntimeException("Can't find Optifine jar file !");
	}
	
	@Nullable
	public static void tryInject(LaunchClassLoader classLoader) {
		try {
			List<IClassTransformer> transformers = $(classLoader, "transformers");
			transformers.remove(1);
			transformers.add(0, new TransformerInjectOptifine(classLoader));
			AlchemyTransformerManager.logger.info(Joiner.on('\n').appendTo(new StringBuilder("Transformers: \n"), transformers).toString());
		} catch (Exception e) { e.printStackTrace(); }
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (!name.startsWith("net.minecraftforge.")) {
			ZipEntry entry = jar.getEntry(name.replace(".", "/") + ".class");
			if (entry != null)
				try {
					return IOUtils.toByteArray(jar.getInputStream(entry));
				} catch (IOException e) { e.printStackTrace(); }
		}
		return basicClass;
	}

}
