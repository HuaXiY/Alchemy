package index.alchemy.dlcs.exnails.core;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import index.alchemy.api.IItemPotion;
import index.alchemy.api.IItemThirst;
import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.Listener;
import index.alchemy.api.annotation.Patch;
import index.alchemy.util.Always;
import index.alchemy.util.Tool;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import toughasnails.api.thirst.ThirstHelper;

import static index.alchemy.util.Tool.$;

@Listener
@Patch("net.minecraft.item.ItemFood")
@Init(state = ModState.POSTINITIALIZED)
public class ExItemFood extends ItemFood implements IItemThirst, IItemPotion {
	
	protected IItemThirst.ThirstMap thirstMap;
	protected IItemThirst.HydrationMap hydrationMap;
	protected IItemPotion.EffectsMap effectsMap;
	
	private ExItemFood(int amount, float saturation, boolean isWolfFood) {
		super(amount, saturation, isWolfFood);
		thirstMap = new IItemThirst.ThirstMap();
		hydrationMap = new IItemThirst.HydrationMap();
		effectsMap = new IItemPotion.EffectsMap();
	}

	@Override
	public int getThirst(ItemStack item) {
		return thirstMap.applyAsInt(item);
	}

	@Override
	public void setThirst(ToIntFunction<ItemStack> handle) {
		if (handle != null)
			thirstMap.handle = handle;
	}
	
	@Override
	public void setThirst(int meta, int thirst) {
		thirstMap.mapping.put(meta, thirst);
	}

	@Override
	public float getHydration(ItemStack item) {
		return hydrationMap.apply(item);
	}

	@Override
	public void setHydration(Function<ItemStack, Float> handle) {
		if (handle != null)
			hydrationMap.handle = handle;
	}
	
	@Override
	public void setHydration(int meta, float hydration) {
		hydrationMap.mapping.put(meta, hydration);
	}
	
	@Override
	public List<PotionEffect> getEffects(ItemStack item) {
		return Tool.isNullOr(effectsMap.apply(item), Lists::newArrayList);
	}

	@Override
	public void setEffects(int meta, List<PotionEffect> effects) {
		effectsMap.mapping.put(meta, effects);
	}

	@Override
	public void setEffects(Function<ItemStack, List<PotionEffect>> handle) {
		if (handle != null)
			effectsMap.handle = handle;
	}
	
	@Override
	public void clearEffects() {
		setPotionEffect(null, 0);
		effectsMap.mapping.clear();
	}
	
	@Patch.Exception
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onFoodEaten(LivingEntityUseItemEvent.Finish event) {
		if (Always.isClient())
			return;
		ItemStack item = event.getItem();
		if (event.getEntityLiving() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.getEntityLiving();
			if (item.getItem() instanceof IItemThirst) {
				IItemThirst thirst = (IItemThirst) item.getItem();
				ThirstHelper.getThirstData(player).addStats(thirst.getThirst(item), thirst.getHydration(item));
			} else {
				ExThirstLoader.ThirstNode node = ExThirstLoader.findNode(item.getItem());
				if (node != null && item.getMetadata() < node.thirst.length)
					ThirstHelper.getThirstData(player).addStats(node.thirst[item.getMetadata()], node.hydration[item.getMetadata()]);
			}
		}
		if (item.getItem() instanceof IItemPotion) {
			IItemPotion potion = (IItemPotion) item.getItem();
			potion.getEffects(item).stream().map(PotionEffect::new).forEach(event.getEntityLiving()::addPotionEffect);
		} else {
			ExPotionLoader.EffectNode node = ExPotionLoader.findNode(item.getItem());
			if (node != null && item.getMetadata() < node.effects.length)
				node.effects[item.getMetadata()].stream().map(PotionEffect::new).forEach(event.getEntityLiving()::addPotionEffect);
		}
	}
	
	@Patch.Exception
	public static void callbackThirst(ItemTooltipEvent event) {
		ItemStack item = event.getItemStack();
		ExThirstLoader.ThirstNode node = ExThirstLoader.findNode(item.getItem());
		if (node != null && item.getMetadata() < node.thirst.length)
			event.getToolTip().add("Thirst: " + node.thirst[item.getMetadata()] + ", " + node.hydration[item.getMetadata()]);
	}
	
	@Patch.Exception
	public static void callbackPotion(ItemTooltipEvent event) {
		ItemStack item = event.getItemStack();
		ExPotionLoader.EffectNode node = ExPotionLoader.findNode(item.getItem());
		if (node != null && item.getMetadata() < node.effects.length)
			event.getToolTip().add("Effect: " +
					Joiner.on("\n    ").appendTo(new StringBuilder("\n    "), node.effects[item.getMetadata()]).toString());
	}
	
	@Patch.Exception
	public static void init() {
		$(IItemThirst.class, "callback<<", (Consumer<ItemTooltipEvent>) ExItemFood::callbackThirst);
		$(IItemPotion.class, "callback<<", (Consumer<ItemTooltipEvent>) ExItemFood::callbackPotion);
	}

}
