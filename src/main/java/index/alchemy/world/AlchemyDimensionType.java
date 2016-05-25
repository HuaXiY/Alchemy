package index.alchemy.world;

import javax.annotation.Nullable;

import index.alchemy.annotation.Config;
import index.alchemy.annotation.Dimension;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.util.Tool;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;

public class AlchemyDimensionType {
	
	@Config(category = "World", comment = "Alchemy mod DimensionType id start")
	private static int alchemy_dimension_type_id = 30;
	private static int next_id = -1;
	
	private static synchronized int nextId() {
		return alchemy_dimension_type_id + ++next_id;
	}
	
	public static void init(Class<?> clazz) {
		AlchemyModLoader.checkState();
		Dimension dimension = clazz.getAnnotation(Dimension.class);
		if (dimension != null && Tool.isSubclass(WorldProvider.class, clazz))
			registerDimensionType(dimension.name(), dimension.suffix(), (Class<? extends WorldProvider>) clazz, dimension.load());
	}
	
	public static void registerDimensionType(String name, String suffix, Class<? extends WorldProvider> provider, boolean load) {
		AlchemyModLoader.checkState();
		DimensionManager.registerDimension(DimensionManager.getNextFreeDimId(), DimensionType.register(name, suffix, nextId(), provider, load));
	}
	
	@Nullable
	public static DimensionType findDimensionType(Class<?> provider) {
		for (DimensionType type : DimensionType.values())
			if (type.getDeclaringClass() == provider)
				return type;
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
