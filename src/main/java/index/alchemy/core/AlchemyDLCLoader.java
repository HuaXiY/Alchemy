package index.alchemy.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.io.Resources;

import index.alchemy.api.IDLCInfo;
import index.alchemy.api.annotation.DLC;
import index.alchemy.api.annotation.Loading;
import index.alchemy.api.event.AlchemyLoadDLCEvent;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.Always;
import index.alchemy.util.AnnotationInvocationHandler;
import index.alchemy.util.Tool;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.asm.transformers.ModAccessTransformer;
import net.minecraftforge.fml.common.event.FMLEvent;

import static index.alchemy.core.AlchemyConstants.*;
import static index.alchemy.util.Tool.$;

@Loading
public class AlchemyDLCLoader {
	
	public static class DLCContainer extends DummyModContainer  {
		
		public final File source;
		
		public DLCContainer(File source, DLC dlc) {
			super(new ModMetadata());
			ModMetadata metadata = getMetadata();
			metadata.modId = MOD_ID + "_dlc_" + dlc.name();
			metadata.name = dlc.name();
			metadata.version = dlc.version();
			metadata.description = dlc.description();
			metadata.parent = MOD_ID;
			this.source = source;
		}
		
		@Override
		public boolean registerBus(EventBus bus, LoadController controller) {
			return true;
		}
		
		@Override
		public File getSource() {
			return source;
		}

		@Override
		public Class<?> getCustomResourcePackClass() {
			try {
				return getSource().isDirectory() ?
						Class.forName("net.minecraftforge.fml.client.FMLFolderResourcePack", true, getClass().getClassLoader()) :
						Class.forName("net.minecraftforge.fml.client.FMLFileResourcePack", true, getClass().getClassLoader());
			} catch (ClassNotFoundException e) {
				return null;
			}
		}

	}
	
	public static final String DESCRIPTOR = Type.getDescriptor(DLC.class), DLCS_PATH = "/mods/dlcs/" + MOD_ID;
	
	private static final Map<String, IDLCInfo> dlc_mapping = Maps.newHashMap();
	
	private static final Map<String, File> file_mapping = Maps.newHashMap();
	
	@Nullable
	public static DLC findDLC(String name) {
		return dlc_mapping.get(name);
	}
	
	public static boolean isDLCLoaded(String name) {
		return findDLC(name) != null;
	}
	
	public static void init(Class<?> clazz) throws IllegalAccessException, InstantiationException {
		AlchemyModLoader.checkState();
		DLC dlc = clazz.getAnnotation(DLC.class);
		if (dlc != null) {
			IDLCInfo info = dlc_mapping.get(dlc.name());
			Object instance = clazz.newInstance();
			for (Method method : clazz.getMethods()) {
				if (!Modifier.isStatic(method.getModifiers()) && method.getReturnType() == void.class) {
					EventHandler handler = method.getAnnotation(EventHandler.class);
					if (handler != null) {
						Class<?> args[] = method.getParameterTypes();
						if (args.length == 1 && Tool.isInstance(FMLEvent.class, args[0])) {
							MethodHandle handle = AlchemyModLoader.lookup.unreflect(method).bindTo(instance);
							AlchemyModLoader.addFMLEventCallback((Class<FMLEvent>) args[0], e -> {
								try {
									handle.invoke(e);
								} catch (Throwable t) {
									AlchemyModLoader.logger.error("In " + method, t);
								}
							});
						}
					}
				}
			}
			if (Always.isClient())
				FMLClientHandler.instance().addModAsResource(info.getDLCContainer());
		}
	}
	
	public static void setup() throws Exception {
		AlchemyModLoader.checkInvokePermissions();
		AlchemyModLoader.checkState();
		
		AlchemyModLoader.logger.info("Setup: " + AlchemyDLCLoader.class.getName());
		
		String val = System.getProperty("index.alchemy.dlcs.bin", "");
		if (!val.isEmpty())
			for (String path : val.split(";"))
				addDLCFile(new File(path.replace("$mc_dir", AlchemyModLoader.mc_dir)));
		else 
			AlchemyModLoader.logger.info("index.alchemy.dlcs.bin is EMPTY");
		
		File dlcs = new File(AlchemyModLoader.mc_dir + DLCS_PATH);
		if (!dlcs.exists())
			dlcs.mkdirs();
		File files[] = dlcs.listFiles();
		if (files != null)
			for (File file : files)
				if (file.getName().endsWith(".dlc"))
					addDLCFile(file);
	}
	
	public static void addDLCFile(File file) {
		AlchemyModLoader.logger.info("Add DLC: " + file.getPath());
		IDLCInfo dlc = null;
		try {
			if ((dlc = checkFileIsDLC(file)) != null) {
				file = update(dlc, file);
				if (MinecraftForge.EVENT_BUS.post(new AlchemyLoadDLCEvent.Pre(dlc, file))) {
					AlchemyModLoader.logger.info("Skip loading DLC: " + file.getPath());
					return;
				}
				if (file.isDirectory()) {
					File meta = new File(file, "META-INF");
					if (meta.isDirectory())
				        for (File at : meta.listFiles()) {
				        	if (at.getName().endsWith("_at.cfg")) {
				        		Map<String, String> embedded = $(ModAccessTransformer.class, "embedded");
				        		embedded.put(String.format("%s/META-INF/%s", file.getPath(), at), Resources.asCharSource(
				        				at.toURI().toURL(), Charsets.UTF_8).read());
				        	}
				        }
				} else
					ModAccessTransformer.addJar(new JarFile(file));
				URL url = file.toURI().toURL();
				Tool.addURLToClassLoader(AlchemyDLCLoader.class.getClassLoader(), url);
				List<String> classes = AlchemyModLoader.findClassFromURL(url);
				AlchemyModLoader.addClass(classes);
				AnnotationInvocationHandler invocationHandler = AnnotationInvocationHandler.asOneOfUs(dlc);
				invocationHandler.memberValues.put("getDLCContainer", new DLCContainer(file, dlc));
				invocationHandler.memberValues.put("getDLCAllClass", ImmutableList.copyOf(classes));
				invocationHandler.memberValues.put("getDLCFile", file);
				file_mapping.put(dlc.name(), file);
				dlc_mapping.put(dlc.name(), dlc);
				MinecraftForge.EVENT_BUS.post(new AlchemyLoadDLCEvent.Post(dlc, file));
				AlchemyModLoader.logger.info("Successfully loaded DLC: " + file.getPath());
			} else
				AlchemyModLoader.logger.warn("DLC: " + file.getPath() + ", is not a standard Alchemy DLC");
		} catch (Exception e) {
			AlchemyModLoader.logger.warn("Failed to load DLC: " + file.getPath(), e);
		}
	}
	
	public static File update(IDLCInfo dlc, File file) {
		if (file.isDirectory())
			return file;
		// TODO
		return file;
	}
	
	@Nullable
	public static IDLCInfo checkFileIsDLC(File file) throws Exception {
		List<IDLCInfo> result = Lists.newLinkedList();
		IDLCInfo dlc;
		if (file.isDirectory()) {
			List<URL> list = new LinkedList<URL>();
			Tool.getAllURL(file, list);
			for (URL url : list)
				if (url.getFile().endsWith(".class") && (dlc = checkClassIsDLC(url.openStream())) != null)
					result.add(dlc);
		} else {
			ZipInputStream input = new ZipInputStream(new FileInputStream(file));
			for (ZipEntry entry; (entry = input.getNextEntry()) != null;)
				if (!entry.isDirectory() && entry.getName().endsWith(".class") && (dlc = checkClassIsDLC(input)) != null)
					result.add(dlc);
		}
		if (result.size() > 1)
			AlchemyRuntimeException.onException(new RuntimeException("This file has multiple DLC"));
		return Tool.getSafe(result, 0);
	}
	
	@Nullable
	public static IDLCInfo checkClassIsDLC(InputStream input) throws Exception {
		try {
			ClassReader reader = new ClassReader(input);
			ClassNode node = new ClassNode(Opcodes.ASM5);
			reader.accept(node, 0);
			if (node.visibleAnnotations != null)
				for (AnnotationNode annotation : node.visibleAnnotations)
					if (DESCRIPTOR.equals(annotation.desc))
						return Tool.makeAnnotation(IDLCInfo.class, makeHandlerMapping(node.name.replace('/', '.')), annotation.values,
								"forgeVersion", "*", "description", "");
		} finally {
			IOUtils.closeQuietly(input);
		}
		return null;
	}
	
	protected static Map<String, InvocationHandler> makeHandlerMapping(String mainClass) {
		Map<String, InvocationHandler> result = Maps.newHashMap();
		result.put("getDLCMainClass", new InvocationHandler() {
			
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				return Tool.forName(mainClass, false);
			}
			
		});
		return result;
	}
	
	public static class GuiAlchemyDLCError extends GuiErrorScreen {

		public GuiAlchemyDLCError(String titleIn, String messageIn) {
			super(null, null);
		}
		
	}

}