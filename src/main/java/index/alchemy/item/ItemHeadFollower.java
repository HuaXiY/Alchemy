package index.alchemy.item;

import org.lwjgl.input.Keyboard;

import baubles.api.BaubleType;
import index.alchemy.api.IFollower;
import index.alchemy.api.annotation.Listener;
import index.alchemy.capability.AlchemyCapabilityLoader;
import index.alchemy.client.AlchemyKeyBinding;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.entity.control.SingleProjection;
import index.alchemy.inventory.InventoryBauble;
import index.alchemy.util.Always;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Listener
public abstract class ItemHeadFollower extends AlchemyItemBauble.AlchemyItemHead {
	
	public static final String KEY_FOLLOWER = "key.follower";
	
	private static long lastTime;
	
	@SideOnly(Side.CLIENT)
	public static final AlchemyKeyBinding
			key_follower = new AlchemyKeyBinding(KEY_FOLLOWER, Keyboard.KEY_G);
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onKeyInput(KeyInputEvent event) {
		if (System.currentTimeMillis() - lastTime > 500 && AlchemyEventSystem.isKeyBindingActive(key_follower)) {
			EntityPlayer player = Minecraft.getMinecraft().player;
			if (player != null) {
				InventoryBauble bauble = player.getCapability(AlchemyCapabilityLoader.bauble, null);
				ItemStack head = bauble.getStackInSlot(BaubleType.HEAD.getValidSlots()[0]);
				if (head != null && head.getItem() instanceof ItemHeadFollower) {
					SingleProjection.cutoverState();
					lastTime = System.currentTimeMillis();
				}
			}
		}
	}
	
	@Override
	public ResourceLocation getResourceLocation() { return getRegistryName(); }
	
	@Override
	@SideOnly(Side.CLIENT)
	public IItemColor getItemColor() { return null; }
	
	@Override
	public void onWornTick(ItemStack item, EntityLivingBase living) {
		if (Always.isServer()) {
			EntityLivingBase follower = IFollower.follower.get(living);
			if (follower == null || follower.isDead || living.getPosition().distanceSq(follower.getPosition()) > 2333) {
				if (follower != null)
					follower.setDead();
				EntityLiving newFollower;
				IFollower.follower.set(living, newFollower = createFollower(item, living));
				((IFollower) newFollower).setOwner(living);
				newFollower.setLocationAndAngles(living.posX, living.posY, living.posZ,
						MathHelper.wrapDegrees(living.world.rand.nextFloat() * 360.0F), 0.0F);
				AlchemyEventSystem.addDelayedRunnable(p -> {
					if (!newFollower.isDead)
						newFollower.world.spawnEntity(newFollower);
				}, 3);
			}
		}
	}
	
	@Override
	public void onUnequipped(ItemStack itemstack, EntityLivingBase living) {
		if (Always.isServer()) {
			EntityLivingBase follower = IFollower.follower.get(living);
			if (follower != null)
				follower.setDead();
			IFollower.follower.set(living, null);
		}
	}
	
	public abstract EntityLiving createFollower(ItemStack item, EntityLivingBase owner);

	public ItemHeadFollower(String name, int color) { super(name, color); }

}
