package index.alchemy.development;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import index.alchemy.annotation.DInit;
import index.alchemy.annotation.Init;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.Constants;
import index.alchemy.util.Tool;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Init(state = ModState.AVAILABLE)
public class DMain {
	
	public static final String
			resources = AlchemyModLoader.mc_dir + "/src/main/resources/assets/" + Constants.MOD_ID,
			PROPERTIES = "build.properties", DEV_VERSION_KEY_NAME = "dev_version";
	
	public static final List<Method> init_obj = new LinkedList<Method>(), init = new LinkedList<Method>();
	
	public static void init(Class<?> clazz) {
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
	}
	
	public static void init(Object obj) {
		for (Method method : init_obj)
			try {
				method.invoke(null, obj);
			} catch (Exception e) {
				AlchemyModLoader.logger.warn("Catch a Exception in init(Object) method with class(" + method.getDeclaringClass().getName() + ")");
				e.printStackTrace();
			}
	}
	
	public static void init() throws IOException {
		for (Method method : init)
			try {
				AlchemyModLoader.logger.info("	init: <" + method.getDeclaringClass() + "> " + method);
				method.invoke(null);
			} catch (Exception e) {
				AlchemyModLoader.logger.warn("Catch a Exception in init method with class(" + method.getDeclaringClass().getName() + ")");
				e.printStackTrace();
			}
		
		updateVersion();
	}
	
	public static void updateVersion() throws IOException {
		Map<String, String> mapping = Tool.getMapping(Tool.readSafe(new File(AlchemyModLoader.mc_dir, PROPERTIES)));
		mapping.put(DEV_VERSION_KEY_NAME, String.valueOf(Integer.valueOf(Tool.isNullOr(mapping.get(DEV_VERSION_KEY_NAME), "0")) + 1));
		StringBuilder builder = new StringBuilder();
		for (Entry<String, String> entry : mapping.entrySet())
			builder.append(entry.getKey()).append('=').append(entry.getValue()).append('\n');
		Tool.saveSafe(new File(AlchemyModLoader.mc_dir, PROPERTIES), builder.toString());
	}

}
