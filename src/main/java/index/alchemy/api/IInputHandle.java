package index.alchemy.api;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IInputHandle {
	
	@SideOnly(Side.CLIENT)
	KeyBinding[] getKeyBindings();
	
}