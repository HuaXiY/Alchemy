package index.alchemy.entity;

import index.alchemy.api.annotation.Hook;
import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.Hook.Type;
import index.project.version.annotation.Omega;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.common.LoaderState.ModState;

import static index.alchemy.util.Tool.*;

@Omega
@Hook.Provider
@Init(state = ModState.PREINITIALIZED)
public class AlchemyDamageSourceLoader {
	
	public static final DamageSource
			soul_withred = new AlchemyDamageSource("soul_withred").setPureDamage().setMagicDamage().setDamageBypassesArmor(),
			dead_magic = new AlchemyDamageSource("dead_magic").setPureDamage().setMagicDamage().setDamageBypassesArmor(),
			plague = new AlchemyDamageSource("plague").setPureDamage().setDissipationDamage().setMagicDamage().setDamageBypassesArmor(),
			alive_power = new AlchemyDamageSource("alive_power").setPureDamage().setMagicDamage().setDamageBypassesArmor();
	
	@Hook(value = "net.minecraft.util.DamageSource#<clinit>", isStatic = true, type = Type.TAIL)
	public static void clinit_DamageSource () {
		$(DamageSource.class, "STARVE<" ,new AlchemyDamageSource(DamageSource.STARVE.getDamageType()).setPureDamage().setDissipationDamage()
				.setMagicDamage().setDamageBypassesArmor().setDamageIsAbsolute());
	}
	
}
