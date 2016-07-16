package index.alchemy.config;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import index.alchemy.api.annotation.Config;
import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.Loading;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.AlchemyConstants;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.Tool;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Loading
@Init(state = ModState.UNLOADED)
public class AlchemyConfigLoader {
	
	public static final String SUFFIX = ".cfg";
	
	public static final String config_dir = AlchemyModLoader.mc_dir + "/config/";
	
	public static final List<Field> config_list = new ArrayList<Field>();
	public static final Map<String, Method> handle_mapping = new HashMap<String, Method>();
	
	@Config(category = "config", comment = "DO NOT MODIFY.")
	private static int version = 1;
	
	@Nullable
	@Deprecated
	private static Configuration configuration;
	
	public static Configuration getConfiguration() {
		if (configuration == null)
			AlchemyRuntimeException.onException(new NullPointerException(AlchemyConfigLoader.class.getName() + ".configuration"));
		return configuration;
	}
		
	public static void init() {
		AlchemyModLoader.checkInvokePermissions();
		AlchemyModLoader.checkState();
		configuration = new Configuration(new File(config_dir + AlchemyConstants.MOD_NAME + SUFFIX));
		getConfiguration().load();
		loadConfiguration();
		getConfiguration().save();
	}

	private static void loadConfiguration() {
		for (Field field : config_list) {
			AlchemyModLoader.logger.info("    init : <" + field.getClass() + "> " + field.toString());
			Config config = field.getAnnotation(Config.class);
			if (config != null) {
				try {
					Method make = null, save = null;
					Class<?> type = null;
					if (config.handle().isEmpty())
						type = field.getType();
					else {
						make = handle_mapping.get(config.handle() + "." + Config.Handle.Type.MAKE.name());
						save = handle_mapping.get(config.handle() + "." + Config.Handle.Type.SAVE.name());
						Tool.checkNull(make, save);
						type = make.getParameterTypes()[0];
					}
					Object value = field.get(null), result = null;
					if (save != null)
						value = save.invoke(null, value);
					if (type == boolean.class)
						result = getConfiguration().getBoolean(field.getName(), config.category(), (Boolean) value, config.comment());
					else if (type == int.class)
						result = getConfiguration().getInt(field.getName(), config.category(), (Integer) value,
								((Number) config.min()).intValue(), ((Number) config.max()).intValue(), config.comment());
					else if (type == float.class)
						result = getConfiguration().getFloat(field.getName(), config.category(), (Float) value,
										config.min(), config.max(), config.comment());
					else if (type == String.class)
						result = getConfiguration().getString(field.getName(), config.category(), (String) value, config.comment());
					if (make != null)
						result = make.invoke(null, result);
					field.set(null, result);
				} catch (Exception e) {
					AlchemyRuntimeException.onException(e);
				}
			}
		}
	}
	
	public static void init(Class<?> clazz) {
		AlchemyModLoader.checkState();
		for (Field field : clazz.getDeclaredFields()) {
			Config config = field.getAnnotation(Config.class);
			if (config != null)
				if (Modifier.isStatic(field.getModifiers()))
					if (config.category() != null) {
						boolean flag = false;
						for (Field f : config_list) {
							Config c = f.getAnnotation(Config.class);
							if (flag |= (c.category().equals(config.category()) && f.getName().equals(field.getName())))
								AlchemyRuntimeException.onException(new RuntimeException(
										"@Config duplicate: " + field.getDeclaringClass().getName() + "#" + field.getName() +
										" <=> " + f.getDeclaringClass().getName() + "#" + f.getName()));
						}
						if (!flag)
							config_list.add(Tool.setAccessible(field));
					} else
						AlchemyRuntimeException.onException(new NullPointerException(
								clazz + "#" + field.getName() + " -> @Config.category()"));
				else
					AlchemyRuntimeException.onException(new IllegalAccessException(
							clazz + "#" + field.getName() + " -> is non static"));
		}
		for (Method method : clazz.getDeclaredMethods()) {
			Config.Handle handle = method.getAnnotation(Config.Handle.class);
			if (handle != null)
				if (Modifier.isStatic(method.getModifiers()))
					if (method.getParameterTypes().length == 1)
						if (handle.name() != null)
							if (handle.type() != null) {
								String name = handle.name() + "." + handle.type().name();
								if (!handle_mapping.containsKey(name))
									handle_mapping.put(name, method);
								else {
									Method old = handle_mapping.get(name);
									AlchemyRuntimeException.onException(new RuntimeException(
											"@Config.Handle duplicate: " + method.getDeclaringClass().getName() + "#" + method.getName() +
											"(" + method.getParameterTypes()[0].getName() + ")" + " <=> " + old.getDeclaringClass().getName() + 
											"#" + old.getName() + "(" + old.getParameterTypes()[0].getName() + ")"
									));
								}
							} else
								AlchemyRuntimeException.onException(new NullPointerException(
										clazz + "#" +  method.getName() + "() -> @Config.Handle.type()"));
						else
							AlchemyRuntimeException.onException(new NullPointerException(
									clazz + "#" +  method.getName() + "() -> @Config.Handle.name()"));
					else
						AlchemyRuntimeException.onException(new IllegalArgumentException(
								clazz + "#" +  method.getName() + "() -> args.length != 1"));
				else
					AlchemyRuntimeException.onException(new IllegalAccessException(
							clazz + "#" + method.getName() + "() -> is non static"));
		}
	}

}
