package index.alchemy.core;

import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Init(state = ModState.PREINITIALIZED)
public class AlchemyDamageSourceLoader {

	public static final DamageSource
			soul_withred = new DamageSource("soul_withred").setMagicDamage().setDamageBypassesArmor().setDamageAllowedInCreativeMode(),
			dead_magic = new DamageSource("dead_magic").setMagicDamage().setDamageBypassesArmor().setDamageAllowedInCreativeMode();
	
	public static void init() {}
	
}
