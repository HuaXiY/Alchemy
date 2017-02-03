package index.alchemy.easteregg;

import index.alchemy.api.annotation.Hook;
import index.alchemy.entity.ai.EntityAIEatMeat;
import net.minecraft.entity.passive.EntityWolf;

@Hook.Provider
public class WolfEatMeat {
	
	@Hook(value = "net.minecraft.entity.passive.EntityWolf#func_184651_r", type = Hook.Type.TAIL)
	public static void initEntityAI(EntityWolf wolf) {
		wolf.tasks.addTask(3, new EntityAIEatMeat(wolf));
	}

}
