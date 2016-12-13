package index.alchemy.entity;

import index.alchemy.api.annotation.Init;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Init(state = ModState.PREINITIALIZED)
public class AlchemyDamageSourceLoader {

	public static final DamageSource
			soul_withred = new AlchemyDamageSource("soul_withred").setMagicDamage().setDamageBypassesArmor(),
			dead_magic = new AlchemyDamageSource("dead_magic").setMagicDamage().setDamageBypassesArmor(),
			plague = new AlchemyDamageSource("plague").setMagicDamage().setDamageBypassesArmor();
	
}
