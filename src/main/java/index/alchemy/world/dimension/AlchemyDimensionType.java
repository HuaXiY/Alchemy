package index.alchemy.world.dimension;

import javax.annotation.Nullable;

import index.alchemy.api.annotation.Config;
import index.alchemy.api.annotation.Dimension;
import index.alchemy.api.annotation.Loading;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.Tool;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;

@Loading
public class AlchemyDimensionType {
	
	@Config(category = "world", comment = "Alchemy mod DimensionType id start.")
	private static int alchemy_dimension_type_id = 30;
	private static int next_id = -1;
	
	private static synchronized int nextId() {
		return alchemy_dimension_type_id + ++next_id;
	}
	
	public static void init(Class<?> clazz) {
		AlchemyModLoader.checkState();
		AlchemyModLoader.checkInvokePermissions();
		Dimension dimension = clazz.getAnnotation(Dimension.class);
		if (dimension != null) {
			if (dimension.name() != null) {
				if (Tool.isSubclass(WorldProvider.class, clazz))
					Tool.setType(clazz, registerDimensionType(dimension.name(), dimension.suffix(),
							(Class<? extends WorldProvider>) clazz, dimension.load()));
				else
					AlchemyRuntimeException.onException(new RuntimeException(
							"Class<" + clazz.getName() + "> forgot to extends the Class<" + WorldProvider.class.getName() + "> ?"));
			} else
				AlchemyRuntimeException.onException(new NullPointerException(clazz + " -> @Dimension.name()"));
		}
	}
	
	public static DimensionType registerDimensionType(String name, String suffix, Class<? extends WorldProvider> provider, boolean load) {
		AlchemyModLoader.checkState();
		return DimensionType.register(name, suffix, nextId(), provider, load);
	}
	
	@Nullable
	public static DimensionType findDimensionType(Class<?> provider) {
		do 
			for (DimensionType type : DimensionType.values())
				if (type.getDeclaringClass() == provider)
					return type;
		while ((provider = provider.getSuperclass()) != null);
		return null;
	}
	
	@Nullable
	public static DimensionType findDimensionType(String name) {
		for (DimensionType type : DimensionType.values())
			if (type.getName().equals(name))
				return type;
		return null;
	}

}