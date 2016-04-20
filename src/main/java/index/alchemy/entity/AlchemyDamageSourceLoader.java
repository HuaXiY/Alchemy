package index.alchemy.entity;

import index.alchemy.core.Init;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Init(state = ModState.PREINITIALIZED)
public class AlchemyDamageSourceLoader {

	public static final DamageSource
			soul_withred = new AlchemyDamageSource("soul_withred").setMagicDamage().setDamageBypassesArmor().setDamageAllowedInCreativeMode(),
			dead_magic = new AlchemyDamageSource("dead_magic").setMagicDamage().setDamageBypassesArmor().setDamageAllowedInCreativeMode();
	
	public static void init() {}
	
}
