package index.alchemy.development;

import java.util.List;

import index.alchemy.api.IEventHandle;
import index.alchemy.api.annotation.Config;
import index.alchemy.api.annotation.Init;
import index.alchemy.util.Tool;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Init(state = ModState.POSTINITIALIZED)
public class RTooltip implements IEventHandle {
	
	@Config(category = "runtime", comment = "Enable item tooltip debbug.")
	private static boolean enable_tooltip_debug = true;
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onItemTooltip(ItemTooltipEvent event) {
		if (enable_tooltip_debug) {
			event.getToolTip().add(event.getItemStack().getItem().getClass().getName());
			addNBTToTooltip(event.getItemStack().getTagCompound(), event.getToolTip(), 1);
		}
	}
	
	public static void addNBTToTooltip(NBTTagCompound nbt, List<String> tooltip, int depth) {
		if (nbt == null)
			return;
		String blank = Tool.makeString(' ', (depth - 1) * 3);
		for (String key : nbt.getKeySet()) {
			NBTBase base = nbt.getTag(key);
			if (base.getId() == NBT.TAG_COMPOUND) {
				tooltip.add(blank + "|-> " + key + " : (" + base.getClass().getSimpleName().replace("NBTTag", "") + ")");
				addNBTToTooltip((NBTTagCompound) base, tooltip, depth + 1);
			} else
				tooltip.add(blank + "|-> " + key + " = (" + base.getClass().getSimpleName().replace("NBTTag", "") + ") " +  base.toString());
		}
	}

}
