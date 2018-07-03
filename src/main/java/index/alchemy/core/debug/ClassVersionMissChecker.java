package index.alchemy.core.debug;

import java.lang.annotation.Annotation;

import index.alchemy.api.annotation.Init;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.util.$;
import index.project.version.annotation.Dev;
import net.minecraftforge.fml.common.LoaderState.ModState;

import static index.alchemy.core.AlchemyConstants.*;

@Dev
@Init(state = ModState.CONSTRUCTED)
public class ClassVersionMissChecker {
	
	public static void init() {
		AlchemyModLoader.getClassList().forEach(ClassVersionMissChecker::checkVersion);
	}
	
	public static void checkVersion(String clazzName) {
		if (clazzName.contains("$") || !clazzName.startsWith(MOD_PACKAGE) || clazzName.startsWith(API_PACKAGE) || clazzName.startsWith(TEST_PACKAGE))
			return;
		Class<?> clazz = $.forName(clazzName, false);
		if (clazz != null) {
			for (Annotation annotation : clazz.getAnnotations())
				if (annotation.annotationType().getName().startsWith("index.project.version.annotation.")) {
					AlchemyModLoader.logger.debug("Version annotation: " + clazzName + " -> " + annotation.getClass().getSimpleName());
					return;
				}
			AlchemyModLoader.logger.warn("Version annotation miss: " + clazzName);
		}
	}

}
