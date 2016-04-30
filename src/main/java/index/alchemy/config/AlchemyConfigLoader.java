package index.alchemy.config;

import java.io.File;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.Constants;
import index.alchemy.core.Init;
import index.alchemy.core.debug.AlchemyRuntimeExcption;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Init(state = ModState.PREINITIALIZED)
public class AlchemyConfigLoader {
	
	public static final String SUFFIX = ".cfg";
	
	public static final String config_dir = AlchemyModLoader.mc_dir + "/config/";
	
	public static final List<Field> config_list = new LinkedList<Field>();
	
	private static Configuration configuration;
		
	public static void init() {
		configuration = new Configuration(new File(config_dir + Constants.MOD_NAME + SUFFIX));
		configuration.load();
		loadConfiguration();
		configuration.save();
	}

	private static void loadConfiguration() {
		for (Field field : config_list) {
			Config config = field.getAnnotation(Config.class);
			if (config != null) {
				try {
					if (field.getType() == int.class)
						field.set(null, Configuration.class.getMethod("getInt", String.class, String.class, int.class, int.class, int.class, String.class)
								.invoke(configuration, field.getName(), config.category(), field.get(null), ((Number) config.min()).intValue(), ((Number) config.max()).intValue(), config.comment()));
					else if (field.getType() == float.class)
						field.set(null, Configuration.class.getMethod("getFloat", String.class, String.class, float.class, float.class, float.class, String.class)
								.invoke(configuration, field.getName(), config.category(), field.get(null), config.min(), config.max(), config.comment()));
					else if (field.getType() == String.class)
						field.set(null, Configuration.class.getMethod("getString", String.class, field.getType(), String.class, String.class)
								.invoke(configuration, field.getName(), config.category(), field.get(null), config.comment()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void init(Class<?> clazz) {
		for (Field field : clazz.getDeclaredFields()) {
			Config config = field.getAnnotation(Config.class);
			if (config != null) {
				for (Field f : config_list) {
					Config c = f.getAnnotation(Config.class);
					if (c.category().equals(config.category()) && f.getName().equals(field.getName()))
						throw new AlchemyRuntimeExcption(new RuntimeException(
								"@Config duplicate: " + field.getDeclaringClass().getName() + field.getName() + "<=>" + f.getDeclaringClass().getName() + f.getName()));
				}
				field.setAccessible(true);
				config_list.add(field);
			}
		}
	}

}
