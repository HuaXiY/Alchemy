package index.alchemy.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.EventBus;

import index.alchemy.api.IDLCInfo;
import index.alchemy.api.annotation.DLC;
import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.Unsafe;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.AnnotationInvocationHandler;
import index.alchemy.util.Tool;
import index.project.version.annotation.Alpha;
import index.project.version.annotation.Beta;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.LoaderState.ModState;

import static index.alchemy.core.AlchemyConstants.*;
import static index.alchemy.util.Tool.$;

@Alpha
@Init(state = ModState.AVAILABLE)
public class AlchemyDLCLoader {
	
	@Beta
	public static class DLCContainer extends DummyModContainer  {
		
		public final IDLCInfo info;
		
		public DLCContainer(IDLCInfo info) {
			super(new ModMetadata());
			ModMetadata metadata = getMetadata();
			metadata.modId = MOD_ID + "_dlc_" + info.name();
			metadata.name = MOD_NAME + " DLC " + info.name();
			metadata.version = info.version();
			metadata.description = info.description();
			metadata.parent = MOD_ID;
			this.info = info;
		}
		
		@Unsafe(Unsafe.REFLECT_API)
		protected void injectLoader() {
			List<ModContainer> mods = $(Loader.instance(), "mods");
			$(Loader.instance(), "mods<", ImmutableList.builder().addAll(mods).add(this).build());
			$(Loader.instance(), "namedMods<", Maps.uniqueIndex(mods, ModContainer::getModId));
			LoadController modController = $(Loader.instance(), "modController");
			Multimap<String, ModState> modStates = $(modController, "modStates");
			modStates.put(getModId(), ModState.AVAILABLE);
			Map<String, String> modNames = $(modController, "modNames");
			modNames.put(getModId(), getName());
			List<ModContainer> activeModList = $(modController, "activeModList");
			activeModList = Lists.newArrayList(activeModList);
			activeModList.add(this);
			$(modController, "activeModList<", activeModList);
			ImmutableMap<String,EventBus> eventChannels = $(modController, "eventChannels");
			$(modController, "eventChannels<", ImmutableMap.builder().putAll(eventChannels).put(getModId(), new EventBus()).build());
		}
		
		@Override
		public boolean registerBus(EventBus bus, LoadController controller) {
			return true;
		}
		
		@Override
		public File getSource() {
			return info.getDLCFile();
		}

		@Override
		public Class<?> getCustomResourcePackClass() {
			return getSource().isDirectory() ?
					Tool.forName("net.minecraftforge.fml.client.FMLFolderResourcePack", true) :
					Tool.forName("net.minecraftforge.fml.client.FMLFileResourcePack", true);
		}
		
		@Override
		public String toString() {
			return MOD_NAME + " DLC: " + info.name();
		}
		
		@Override
		public Disableable canBeDisabled() {
			return Disableable.RESTART;
		}

	}
	
	public static final String DESCRIPTOR = Type.getDescriptor(DLC.class), DLCS_PATH = "/mods/dlcs/" + MOD_ID;
	
	private static final Logger logger = LogManager.getLogger(AlchemyDLCLoader.class.getSimpleName());
	
	private static final String mc_dir = AlchemyEngine.getMinecraftDir().getPath();
	
	private static final Map<String, IDLCInfo> dlc_mapping = Maps.newHashMap();
	
	private static final Map<String, File> file_mapping = Maps.newHashMap();
	
	@Nullable
	public static IDLCInfo findDLC(String name) { return dlc_mapping.get(name); }
	
	public static boolean isDLCLoaded(String name) { return findDLC(name) != null; }
	
	public static Stream<IDLCInfo> stream() { return dlc_mapping.values().stream(); }
	
	protected static void injectLoader() {
		dlc_mapping.values().stream().map(IDLCInfo::getDLCContainer).forEach(DLCContainer::injectLoader);
	}
	
	public static void init() { injectLoader(); }
	
	public static void setup() {
		AlchemyEngine.checkInvokePermissions();
		logger.info("Setup: " + AlchemyDLCLoader.class.getName());
		
		String val = System.getProperty("index.alchemy.dlcs.bin", "");
		if (!val.isEmpty())
			for (String path : val.split(";"))
				addDLCFile(new File(path.replace("$mc_dir", mc_dir)));
		else 
			logger.info("index.alchemy.dlcs.bin is EMPTY");
		
		File dlcs = new File(mc_dir + DLCS_PATH);
		if (!dlcs.exists())
			dlcs.mkdirs();
		File files[] = dlcs.listFiles();
		if (files != null)
			for (File file : files)
				if (file.getName().endsWith(".dlc"))
					addDLCFile(file);
	}
	
	private static void addDLCFile(File file) {
		logger.info("Add DLC: " + file.getPath());
		IDLCInfo dlc = null;
		try {
			if ((dlc = checkFileIsDLC(file)) != null) {
				if (file.isFile()) {
					LaunchClassLoader loader = AlchemyEngine.getLaunchClassLoader();
					AlchemySetup.injectAccessTransformer(file, loader);
					AlchemySetup.injectAccessTransformer(file, dlc.id() + "_at.cfg", loader);
				}
				URL url = file.toURI().toURL();
				Tool.addURLToClassLoader(AlchemyDLCLoader.class.getClassLoader(), url);
				AnnotationInvocationHandler invocationHandler = AnnotationInvocationHandler.asOneOfUs(dlc);
				invocationHandler.memberValues.put("getDLCContainer", new DLCContainer(dlc));
				invocationHandler.memberValues.put("getDLCAllClass", ImmutableList.copyOf(AlchemyEngine.findClassFromURL(url)));
				invocationHandler.memberValues.put("getDLCFile", file);
				file_mapping.put(dlc.name(), file);
				dlc_mapping.put(dlc.name(), dlc);
				logger.info("Successfully loaded DLC: " + file.getPath());
			} else
				logger.warn("DLC: " + file.getPath() + ", is not a standard Alchemy DLC");
		} catch (Exception e) {
			logger.warn("Failed to load DLC: " + file.getPath(), e);
		}
	}
	
	@Nullable
	private static IDLCInfo checkFileIsDLC(File file) throws Exception {
		List<IDLCInfo> result = Lists.newLinkedList();
		IDLCInfo dlc;
		if (file.isDirectory()) {
			for (URL url : Tool.getAllURL(file, Lists.newLinkedList()))
				if (url.getFile().endsWith(".class") && (dlc = checkClassIsDLC(url.openStream())) != null)
					result.add(dlc);
		} else
			try (ZipInputStream input = new ZipInputStream(new FileInputStream(file))) {
				for (ZipEntry entry; (entry = input.getNextEntry()) != null;)
					if (!entry.isDirectory() && entry.getName().endsWith(".class") && (dlc = checkClassIsDLC(input)) != null)
						result.add(dlc);
			}
		if (result.size() > 1)
			AlchemyRuntimeException.onException(new RuntimeException("This file(" + file + ") has multiple DLC"));
		return Tool.getSafe(result, 0);
	}
	
	@Nullable
	private static IDLCInfo checkClassIsDLC(InputStream input) throws Exception {
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
			if (!(input instanceof ZipInputStream))
				IOUtils.closeQuietly(input);
		}
		return null;
	}
	
	private static Map<String, InvocationHandler> makeHandlerMapping(String mainClass) {
		Map<String, InvocationHandler> result = Maps.newHashMap();
		result.put("getDLCMainClass", (proxy, method, args) -> Tool.forName(mainClass, false));
		result.put("clinitDLCMainClass", (proxy, method, args) -> Tool.forName(mainClass, true));
		return result;
	}
	
}