package index.alchemy.development;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.relauncher.Side;
import index.alchemy.api.Alway;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.Constants;
import index.alchemy.core.Init;

@Init(state = ModState.AVAILABLE)
public class DMain {
	
	public static final String resources = AlchemyModLoader.mc_dir + "/src/main/resources/assets/" + Constants.MODID;
	
	public static final List<Method> init_obj = new LinkedList<Method>(), init = new LinkedList<Method>();
	
	public static void init(Class<?> clazz) {
		if (Alway.getSide() == Side.CLIENT)
			for (DInit dinit : clazz.getAnnotationsByType(DInit.class)) {
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
				AlchemyModLoader.logger.warn("Catch a Exception in init method with class(" + method.getDeclaringClass().getName() + ")");
				e.printStackTrace();
			}
	}
	
	public static void init() {
		for (Method method : init)
			try {
				AlchemyModLoader.logger.info("    init: <" + method.getDeclaringClass() + "> " + method);
				method.invoke(null);
			} catch (Exception e) {
				AlchemyModLoader.logger.warn("Catch a Exception in init method with class(" + method.getDeclaringClass().getName() + ")");
				e.printStackTrace();
			}
	}

}
