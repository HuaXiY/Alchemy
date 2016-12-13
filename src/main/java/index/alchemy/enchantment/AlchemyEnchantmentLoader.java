package index.alchemy.enchantment;

import index.alchemy.api.annotation.Init;
import net.minecraft.enchantment.Enchantment;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Init(state = ModState.PREINITIALIZED)
public class AlchemyEnchantmentLoader {
	
	public static final Enchantment
			siphon_life = new EnchantmentSiphonLife(),
			double_jump = new EnchantmentDoubleJump(),
			phase_shift = new EnchantmentPhaseShift();
	
}
