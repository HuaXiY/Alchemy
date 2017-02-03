package index.alchemy.client.fx.update;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import index.alchemy.api.IFXUpdate;
import index.alchemy.api.annotation.FX;
import index.alchemy.api.annotation.Loading;
import index.alchemy.core.AlchemyCorePlugin;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.Tool;
import index.project.version.annotation.Omega;

@Omega
@Loading
public class FXUpdateHelper {
	
	private static final List<String> strings = Lists.newArrayList();
	private static final List<Function<int[], List<IFXUpdate>>> functions = Lists.newArrayList();
	
	public static synchronized void init(Class<?> clazz) {
		AlchemyModLoader.checkState();
		FX.UpdateProvider provider = clazz.getAnnotation(FX.UpdateProvider.class);
		if (provider != null)
			for (Method method : clazz.getMethods()) {
				FX.UpdateMethod m = method.getAnnotation(FX.UpdateMethod.class);
				if (m != null)
					if (m.value() != null)
						if (Modifier.isStatic(method.getModifiers()))
							if (method.getParameterTypes().length == 1 
							&& method.getParameterTypes()[0].isArray() && method.getParameterTypes()[0].getComponentType() == int.class)
								if (method.getReturnType() == List.class) {
									strings.add(m.value());
									functions.add(AlchemyCorePlugin.getASMClassLoader().createWrapper(method, null));
								} else
									AlchemyRuntimeException.onException(new ClassCastException(
											clazz + "#" + method.getName() + "() -> return type != " + List.class.getName()));
							else
								AlchemyRuntimeException.onException(new IllegalArgumentException(
										clazz + "#" + method.getName() + "() -> args != int[]"));
						else
							AlchemyRuntimeException.onException(new IllegalAccessException(
									clazz + "#" + method.getName() + "() -> is non static"));
					else
						AlchemyRuntimeException.onException(new NullPointerException(
								clazz + "#" + method.getName() + "() -> @FX.UpdateMethod.value()"));
			}
	}
	
	public static int getIdByName(String name) {
		return strings.indexOf(name);
	}
	
	public static int[] getIntArrayByArgs(String name, int... args) {
		int result[] = new int[args.length + 1];
		result[0] = getIdByName(name);
		System.arraycopy(args, 0, result, 1, args.length);
		return result;
	}
	
	public static List<IFXUpdate> getResultByArgs(int... args) {
		if (args.length != 0)
			try {
				return (List<IFXUpdate>) Tool.getSafe(functions, args[0]).apply(args);
			} catch (Exception e) { AlchemyRuntimeException.onException(e); }
		return Lists.newLinkedList();
	}
	
}