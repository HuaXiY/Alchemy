package index.alchemy.core;

import com.google.common.collect.*;
import com.google.common.eventbus.EventBus;
import index.alchemy.api.IDLCInfo;
import index.alchemy.api.annotation.DLC;
import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.Unsafe;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.*;
import index.project.version.annotation.Alpha;
import index.project.version.annotation.Beta;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.LoaderState.ModState;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static index.alchemy.core.AlchemyConstants.MOD_ID;
import static index.alchemy.core.AlchemyConstants.MOD_NAME;
import static index.alchemy.util.$.$;

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
			Map<ModContainer, Object> modObjectList = $(modController, "modObjectList");
			$(modController, "modObjectList<", ImmutableBiMap.builder().putAll(modObjectList).put(this, info).build());
			ListMultimap<String, ModContainer> packageOwners = $(modController, "packageOwners");
			info.getDLCAllPackage().forEach(packageName -> packageOwners.put(packageName, this));
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
			String path = info.getDLCURL().getFile();
			return path != null ? new File(path) : null;
		}
		
		@Override
		public Object getMod() {
			return info;
		}

		@Override
		public Class<?> getCustomResourcePackClass() {
			return getSource().isDirectory() ?
					$.forName("net.minecraftforge.fml.client.FMLFolderResourcePack", true) :
					$.forName("net.minecraftforge.fml.client.FMLFileResourcePack", true);
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
	
	public static class GUICheckDLCLoader {
		
		public static void show(Collection<IDLCInfo> dlcInfos) {
			JFXHelper.runAndWait(() -> {
				ListView<IDLCInfo> listView = new ListView<>();
				listView.getItems().addAll(dlcInfos);
				listView.setCellFactory(CheckBoxListCell.forListView(item -> {
					BooleanProperty observable = new SimpleBooleanProperty(Boolean.TRUE);
					observable.addListener((obs, wasSelected, isNowSelected) -> item.state().setValue(isNowSelected));
					return observable;
				}));
				listView.setMinWidth(800.0);
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.initOwner(null);
				alert.setTitle(GUICheckDLCLoader.class.getEnclosingClass().getSimpleName());
				alert.setHeaderText("Check the DLC to load.");
				GridPane dlcList = new GridPane();
				dlcList.add(listView, 0, 0);
				alert.getDialogPane().setContent(dlcList);
				Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
				stage.setAlwaysOnTop(true);
				stage.getIcons().add(new Image(GUICheckDLCLoader.class.getResourceAsStream("/dlc.png")));
				alert.setWidth(800);
				alert.showAndWait();
			});
		}
		
	}
	
	public static final String DESCRIPTOR = Type.getDescriptor(DLC.class), DLCS_PATH = "/mods/dlcs/" + MOD_ID, DEV_DLCS_DIR = "/dlc/";
	
	private static final Logger logger = LogManager.getLogger(AlchemyDLCLoader.class.getSimpleName());
	
	private static final String mc_dir = AlchemyEngine.getMinecraftDir().getPath();
	
	private static final Map<String, IDLCInfo> dlc_mapping = Maps.newHashMap();
	
	@Nullable
	public static IDLCInfo findDLC(String id) { return dlc_mapping.get(id); }
	
	public static boolean isDLCLoaded(String id) { return findDLC(id) != null; }
	
	public static Stream<IDLCInfo> stream() { return dlc_mapping.values().stream(); }
	
	protected static void injectLoader() {
		dlc_mapping.values().stream()
			.filter(IDLCInfo::shouldInjectLoader)
			.map(IDLCInfo::getDLCContainer)
			.forEach(DLCContainer::injectLoader);
	}
	
	public static void init() { injectLoader(); }
	
	public static void setup() {
		AlchemyEngine.checkInvokePermissions();
		logger.info("Setup: " + AlchemyDLCLoader.class.getName());
		
		String val = System.getProperty("index.alchemy.dlcs.bin", ""),
				mc_dir = ".".equals(AlchemyDLCLoader.mc_dir) ? ".." : AlchemyDLCLoader.mc_dir;
		if (!val.isEmpty())
			for (String path : val.split(";"))
				addDLCFile(new File(path.replace("$mc_dir", mc_dir)));
		else 
			logger.info("index.alchemy.dlcs.bin is EMPTY");
		
		if (!AlchemyEngine.isRuntimeDeobfuscationEnabled()) {
			File dlcs = new File(mc_dir + DEV_DLCS_DIR);
			if (!dlcs.exists())
				dlcs.mkdirs();
			File files[] = dlcs.listFiles();
			if (files != null)
				for (File dir : files)
					if (dir.isDirectory()) {
//                        dir =
                        addDLCFile(dir);
                    }
		}
		
		{
			File dlcs = new File(mc_dir + DLCS_PATH);
			if (!dlcs.exists())
				dlcs.mkdirs();
			File files[] = dlcs.listFiles();
			if (files != null)
				for (File file : files)
					if (file.getName().endsWith(".dlc"))
						addDLCFile(file);
		}
		
		GUICheckDLCLoader.show(dlc_mapping.values());
		
		dlc_mapping.values().stream()
			.filter(IDLCInfo::shouldLoad)
			.peek(dlc -> Tool.addURLToClassLoader(AlchemyDLCLoader.class.getClassLoader(), dlc.getDLCURL()))
			.forEach(dlc -> logger.info("Load dlc: " + dlc));
	}
	
	private static void addDLCFile(File file) {
		if (!file.exists()) {
			logger.error("The specified file does not exist: " + file.getPath());
			return;
		}
		logger.info("Add DLC: " + file.getPath());
		IDLCInfo dlc;
		try {
			if ((dlc = checkFileIsDLC(file)) != null) {
				if (file.isFile())
					AlchemySetup.injectAccessTransformer(file, AlchemyEngine.getLaunchClassLoader());
				URL url = file.toURI().toURL();
				Set<String> classSet = AlchemyEngine.findClassFromURL(url);
				AnnotationInvocationHandler invocationHandler = AnnotationInvocationHandler.asOneOfUs(dlc);
				invocationHandler.memberValues.put("getDLCContainer", new DLCContainer(dlc));
				invocationHandler.memberValues.put("getDLCAllClass", ImmutableSet.copyOf(classSet));
				invocationHandler.memberValues.put("getDLCAllPackage", ImmutableSet.copyOf(classSet.stream()
						.map(clazz -> Tool.get(clazz, "(.*)\\."))
						.filter(Sets.newHashSet()::add)
						.collect(Collectors.toList())));
				invocationHandler.memberValues.put("getDLCURL", url);
				dlc_mapping.put(dlc.id(), dlc);
				logger.info("Successfully loaded DLC: " + file.getPath());
				logger.info("DLC info: " + dlc.getInfo());
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
								"state", new Pointer<>(Boolean.TRUE), "forgeVersion", "*", "description", "");
		} finally { 
			if (!(input instanceof ZipInputStream))
				IOUtils.closeQuietly(input);
		}
		return null;
	}
	
	private static Map<String, InvocationHandler> makeHandlerMapping(String mainClass) {
		Map<String, InvocationHandler> result = Maps.newHashMap();
		result.put("getDLCMainClass", (proxy, method, args) -> $.forName(mainClass, false));
		result.put("clinitDLCMainClass", (proxy, method, args) -> $.forName(mainClass, true));
		result.put("toString", (proxy, method, args) -> IDLCInfo.class.cast(proxy).getInfo());
		return result;
	}
	
}