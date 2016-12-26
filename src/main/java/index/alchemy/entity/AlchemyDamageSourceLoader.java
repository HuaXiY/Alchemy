package index.alchemy.entity;

import index.alchemy.api.annotation.Init;
import index.project.version.annotation.Omega;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Omega
@Init(state = ModState.PREINITIALIZED)
public class AlchemyDamageSourceLoader {
	
	public static final DamageSource
			soul_withred = new AlchemyDamageSource("soul_withred").setPureDamage().setMagicDamage().setDamageBypassesArmor(),
			dead_magic = new AlchemyDamageSource("dead_magic").setMagicDamage().setDamageBypassesArmor(),
			plague = new AlchemyDamageSource("plague").setPureDamage().setMagicDamage().setDamageBypassesArmor(),
			alive_power = new AlchemyDamageSource("alive_power").setPureDamage().setMagicDamage().setDamageBypassesArmor();
	
}
