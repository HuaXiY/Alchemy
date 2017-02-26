package index.alchemy.core.asm.transformer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.commons.io.IOUtils;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import index.alchemy.api.annotation.Hook;
import index.alchemy.core.AlchemyEngine;
import index.alchemy.util.ReflectionHelper;
import index.project.version.annotation.Dev;
import index.project.version.annotation.Omega;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static index.alchemy.util.Tool.$;

@Dev
@Omega
@Hook.Provider
@SideOnly(Side.CLIENT)
public class TransformerInjectOptifine implements IClassTransformer {
	
	public static final String OPTIFINE_INFO = "[OptiFine] [INFO] (Reflector) ";
	
	protected JarFile jar;
	
	public static void tryInject(LaunchClassLoader classLoader) {
		try {
			List<IClassTransformer> transformers = $(classLoader, "transformers");
			transformers.removeIf(t -> t.getClass().getName().startsWith("optifine."));
			transformers.add(0, new TransformerInjectOptifine(classLoader));
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	@Hook(value = "Reflector#<clinit>", isStatic = true, type = Hook.Type.TAIL)
	public static void clinit() {
		try {
			Method target;
			$($("LReflector", "ForgeBlock_getLightOpacity"), "targetMethod<", target = ReflectionHelper.getMethod(Block.class,
					"getLightOpacity", IBlockState.class, IBlockAccess.class, BlockPos.class));
			AlchemyEngine.sysout.println(OPTIFINE_INFO + "Set method net.minecraft.block.Block.getLightOpacity -> " + target);
			$($("LReflector", "ForgeBlock_getLightValue"), "targetMethod<", target = ReflectionHelper.getMethod(Block.class,
					"getLightValue", IBlockState.class, IBlockAccess.class, BlockPos.class));
			AlchemyEngine.sysout.println(OPTIFINE_INFO + "Set method net.minecraft.block.Block.getLightValue -> " + target);
		} catch(Exception e) { new RuntimeException("Can't set optifine Reflector", e).printStackTrace(AlchemyEngine.syserr); }
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (transformedName.startsWith("net.minecraft.world.chunk.") || transformedName.startsWith("net.minecraft.server.integrated.") ||
				transformedName.startsWith("net.minecraft.client.multiplaye."))
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

}
