package index.alchemy.achievement;

import java.util.function.Predicate;

import index.alchemy.api.IEventHandle;
import index.alchemy.util.InventoryHelper;
import index.project.version.annotation.Omega;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Omega
public class AchievementUseItem extends AlchemyAchievement implements IEventHandle {
	
	private Predicate<ItemStack> item;
	
	public AchievementUseItem(String name, int column, int row, Item icon, Achievement parent) {
		this(name, column, row, icon, icon.getClass(), parent);
		item = i -> i.getItem() == item;
	}
	
	public AchievementUseItem(String name, int column, int row, Item icon, Class<? extends Item> clazz, Achievement parent) {
		super(name, column, row, icon, parent);
		item = i -> i.getItem().getClass() == clazz;
	}
	
	public AchievementUseItem(String name, int column, int row, Item icon, ItemStack stack, Achievement parent) {
		super(name, column, row, icon, parent);
		item = i -> InventoryHelper.areItemsMetaEqual(stack, i);
	}

	@SubscribeEvent
	public void onLivingEntityUseItem_Finish(LivingEntityUseItemEvent.Finish event) {
		if (event.getEntityLiving() instanceof EntityPlayer && item.test(event.getItem()))
			((EntityPlayer) event.getEntityLiving()).addStat(this);
	}

}