package index.alchemy.development;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import index.alchemy.api.annotation.DInit;
import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.Lang;
import index.alchemy.api.annotation.Loading;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.core.AlchemyConstants;
import index.alchemy.util.Tool;
import index.project.version.annotation.Alpha;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Alpha
@Loading
@SideOnly(Side.CLIENT)
@Init(state = ModState.AVAILABLE)
public class DMain {
	
	public static final String
			resources = AlchemyModLoader.mc_dir + "/src/main/resources/assets/" + AlchemyConstants.MOD_ID,
			PROPERTIES = "build.properties", DEV_VERSION_KEY_NAME = "dev_version";
	
	public static final List<Method> init_obj = new ArrayList<Method>(), init = new ArrayList<Method>();
	
	public static void init(Class<?> clazz) {
		if (!AlchemyModLoader.enable_dmain)
			return;
		AlchemyModLoader.checkState();
		DInit dInit = clazz.getAnnotation(DInit.class);
		if (dInit != null) {
			try {
				init_obj.add(clazz.getMethod("init", Object.class));
			} catch (NoSuchMethodException e) {
				AlchemyModLoader.logger.warn("Can't find init(Object) method in: " + clazz.getName());
			}
			try {
				init.add(clazz.getMethod("init"));
			} catch (NoSuchMethodException e) {
				AlchemyModLoader.logger.warn("Can't find init method in: " + clazz.getName());
			}
		}
		Lang lang = clazz.getAnnotation(Lang.class);
		if (lang != null && Tool.isSubclass(Enum.class, clazz)) {
			try {
				Enum[] enums = (Enum[]) Tool.setAccessible(clazz.getDeclaredField("ENUM$VALUES")).get(null);
				String prefix = (String) Tool.setAccessible(clazz.getDeclaredField("PREFIX")).get(null);
				for (Enum e : enums)
					DLang.miscMap.put(prefix + e.name().toLowerCase(), e.name().toLowerCase());
			} catch (Exception e) {
				AlchemyRuntimeException.onException(e);
			}
		}
	}
	
	public static void init(Object obj) {
		AlchemyModLoader.checkState();
		if (obj.getClass().getName().startsWith("index.alchemy."))
			for (Method method : init_obj)
				try {
					method.invoke(null, obj);
				} catch (Exception e) {
					AlchemyModLoader.logger.warn("Catch a Exception in init(Object) method with class(" + method.getDeclaringClass().getName() + ")");
					e.printStackTrace();
				}
	}
	
	public static void init() throws IOException {
		AlchemyModLoader.checkInvokePermissions();
		AlchemyModLoader.checkState(ModState.AVAILABLE);
		for (Method method : init)
			try {
				AlchemyModLoader.info(method.getDeclaringClass(), method);
				method.invoke(null);
			} catch (Exception e) {
				AlchemyModLoader.logger.warn("Catch a Exception in init method with class(" + method.getDeclaringClass().getName() + ")");
				e.printStackTrace();
			}
		updateVersion();
	}
	
	private static void updateVersion() throws IOException {
		Map<String, String> mapping = Tool.getMapping(Tool.readSafe(new File(AlchemyModLoader.mc_dir, PROPERTIES)));
		mapping.put(DEV_VERSION_KEY_NAME, String.valueOf(Integer.valueOf(Tool.isNullOr(mapping.get(DEV_VERSION_KEY_NAME), "0")) + 1));
		StringBuilder builder = new StringBuilder();
		for (Entry<String, String> entry : mapping.entrySet())
			builder.append(entry.getKey()).append('=').append(entry.getValue()).append('\n');
		Tool.saveSafe(new File(AlchemyModLoader.mc_dir, PROPERTIES), builder.toString());
	}

}
