package index.alchemy.dlcs.exnails.core;

import java.util.function.Function;
import java.util.function.ToIntFunction;

import index.alchemy.api.IItemThirst;
import index.alchemy.api.annotation.Listener;
import index.alchemy.api.annotation.Patch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import toughasnails.api.thirst.ThirstHelper;

@Listener
@Patch("net.minecraft.item.ItemFood")
public class ExItemFood extends ItemFood implements IItemThirst {
	
	protected ToIntFunction<ItemStack> thirst;
	protected Function<ItemStack, Float> hydration;
	
	private ExItemFood(int amount, float saturation, boolean isWolfFood) {
		super(amount, saturation, isWolfFood);
		thirst = i -> 0;
		hydration = i -> 0F;
	}

	@Override
	public int getThirst(ItemStack item) {
		return thirst.applyAsInt(item);
	}

	@Override
	public void setThirst(ToIntFunction<ItemStack> handle) {
		thirst = handle;
	}

	@Override
	public float getHydration(ItemStack item) {
		return hydration.apply(item);
	}

	@Override
	public void setHydration(Function<ItemStack, Float> handle) {
		hydration = handle;
	}
	
	@Patch.Exception
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onFoodEaten(LivingEntityUseItemEvent.Finish event) {
		if (event.getItem().getItem() instanceof IItemThirst && event.getEntityLiving() instanceof EntityPlayer) {
			ItemStack item = event.getItem();
			IItemThirst thirst = (IItemThirst) item.getItem();
			EntityPlayer player = (EntityPlayer) event.getEntityLiving();
			ThirstHelper.getThirstData(player).addStats(thirst.getThirst(item), thirst.getHydration(item));
		}
	}

}
