package index.alchemy.entity;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import index.alchemy.api.annotation.Init;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.util.Tool;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Init(state = ModState.AVAILABLE)
public class AlchemyEntityManager {
	
	public static List<Class<? extends EntityLivingBase>> 
			FRIENDLY_LIVING_LIST = new ArrayList<Class<? extends EntityLivingBase>>(),
			MONSTER_LIST = new ArrayList<Class<? extends EntityLivingBase>>();
	
	public static EntityLivingBase getRandomEntity(List<Class<? extends EntityLivingBase>> list, World world) {
		return getEntityById(list, AlchemyModLoader.random.nextInt(list.size()), world);
	}
	
	public static EntityLivingBase getEntityById(List<Class<? extends EntityLivingBase>> list, int id, World world) {
		Class<? extends EntityLivingBase> clazz = list.get(id);
		try {
			return list.get(id).getConstructor(World.class).newInstance(world);
		} catch (Exception e) {
			AlchemyModLoader.logger.warn("Can't be instantiated: " + clazz.getName(), e);
			return new EntityPig(world);
		}
	}
	
	public static void init() {
		for (Class<? extends Entity> clazz : EntityList.CLASS_TO_NAME.keySet())
			if (Tool.isSubclass(EntityLivingBase.class, clazz) && !Modifier.isAbstract(clazz.getModifiers()))
				(Tool.isSubclass(EntityAnimal.class, clazz) ? FRIENDLY_LIVING_LIST : MONSTER_LIST)
					.add((Class<? extends EntityLivingBase>) clazz);
	}
	
}