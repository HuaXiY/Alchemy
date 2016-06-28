package index.alchemy.config;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import index.alchemy.api.annotation.Config;
import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.Loading;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.Constants;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.Tool;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Loading
@Init(state = ModState.PREINITIALIZED)
public class AlchemyConfigLoader {
	
	public static final String SUFFIX = ".cfg";
	
	public static final String config_dir = AlchemyModLoader.mc_dir + "/config/";
	
	public static final List<Field> config_list = new ArrayList<Field>();
	
	@Config(category = "config", comment = "DO NOT MODIFY.")
	private static int version = 1;
	
	private static Configuration configuration;
		
	public static void init() {
		AlchemyModLoader.checkInvokePermissions();
		AlchemyModLoader.checkState();
		configuration = new Configuration(new File(config_dir + Constants.MOD_NAME + SUFFIX));
		configuration.load();
		loadConfiguration();
		configuration.save();
	}

	private static void loadConfiguration() {
		for (Field field : config_list) {
			AlchemyModLoader.logger.info("    init : <" + field.getClass() + "> " + field.toString());
			Config config = field.getAnnotation(Config.class);
			if (config != null) {
				try {
					if (field.getType() == boolean.class)
						field.set(null, Configuration.class.getMethod("getBoolean",
								String.class, String.class, boolean.class, String.class)
								.invoke(configuration, field.getName(), config.category(), field.get(null), config.comment()));
					else if (field.getType() == int.class)
						field.set(null, Configuration.class.getMethod("getInt",
								String.class, String.class, int.class, int.class, int.class, String.class)
								.invoke(configuration, field.getName(), config.category(), field.get(null),
										((Number) config.min()).intValue(), ((Number) config.max()).intValue(), config.comment()));
					else if (field.getType() == float.class)
						field.set(null, Configuration.class.getMethod("getFloat",
								String.class, String.class, float.class, float.class, float.class, String.class)
								.invoke(configuration, field.getName(), config.category(), field.get(null),
										config.min(), config.max(), config.comment()));
					else if (field.getType() == String.class)
						field.set(null, Configuration.class.getMethod("getString",
								String.class, field.getType(), String.class, String.class)
								.invoke(configuration, field.getName(), config.category(), field.get(null), config.comment()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void init(Class<?> clazz) {
		AlchemyModLoader.checkState();
		for (Field field : clazz.getDeclaredFields()) {
			Config config = field.getAnnotation(Config.class);
			if (config != null) {
				if (config.category() != null) {
					for (Field f : config_list) {
						Config c = f.getAnnotation(Config.class);
						if (c.category().equals(config.category()) && f.getName().equals(field.getName()))
							AlchemyRuntimeException.onException(new RuntimeException(
									"@Config duplicate: " + field.getDeclaringClass().getName() + "#" + field.getName() +
									"<=>" + f.getDeclaringClass().getName() + "#" + f.getName()));
					}
					config_list.add(Tool.setAccessible(field));
				} else
					AlchemyRuntimeException.onException(new NullPointerException(
							clazz + "#" + field.getName() + " -> @Config.category()"));
			}
		}
	}

}
