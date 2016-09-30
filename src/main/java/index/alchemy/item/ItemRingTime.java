package index.alchemy.item;

import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import index.alchemy.animation.StdCycle;
import index.alchemy.api.Always;
import index.alchemy.api.IContinuedRunnable;
import index.alchemy.api.ICoolDown;
import index.alchemy.api.IFXUpdate;
import index.alchemy.api.IInputHandle;
import index.alchemy.api.INetworkMessage;
import index.alchemy.api.IPhaseRunnable;
import index.alchemy.api.annotation.FX;
import index.alchemy.api.annotation.KeyEvent;
import index.alchemy.capability.AlchemyCapabilityLoader;
import index.alchemy.capability.CapabilityTimeLeap.TimeSnapshot;
import index.alchemy.capability.CapabilityTimeLeap.TimeSnapshot.TimeNode;
import index.alchemy.client.AlchemyKeyBinding;
import index.alchemy.client.color.ColorHelper;
import index.alchemy.client.fx.FXWisp;
import index.alchemy.client.fx.update.FXARGBIteratorUpdate;
import index.alchemy.client.fx.update.FXAgeUpdate;
import index.alchemy.client.fx.update.FXBrightnessUpdate;
import index.alchemy.client.fx.update.FXScaleUpdate;
import index.alchemy.client.fx.update.FXUpdateHelper;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.item.AlchemyItemBauble.AlchemyItemRing;
import index.alchemy.item.ItemRingTime.MessageTimeLeap;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.network.Double6IntArrayPackage;
import index.alchemy.util.AABBHelper;
import index.alchemy.util.Tool;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import static java.lang.Math.*;

@FX.UpdateProvider
public class ItemRingTime extends AlchemyItemRing implements IInputHandle, INetworkMessage.Server<MessageTimeLeap>, ICoolDown {
	
	public static final int USE_CD = 20 * 20, REPAIR_INTERVAL = 20 * 10;
	public static final String NBT_KEY_CD = "cd_ring_time", KEY_DESCRIPTION = "key.time_ring_leap", FX_KEY_GATHER = "ring_time_gather";
	
	@FX.UpdateMethod(FX_KEY_GATHER)
	public static List<IFXUpdate> getFXUpdateGather(int[] args) {
		List<IFXUpdate> result = new LinkedList<IFXUpdate>();
		int i = 1, 
			max_age = Tool.getSafe(args, i++, 120),
			scale = Tool.getSafe(args, i++, 100);
		result.add(new FXAgeUpdate(max_age));
		result.add(new FXBrightnessUpdate(0xF << 20 | 0xF << 4));
		result.add(new FXARGBIteratorUpdate(ColorHelper.ahsbStep(new Color(0x66, 0xCC, 0xFF), new Color(0xFF, 0, 0, 0x22),
				max_age, true, true, false)));
		result.add(new FXScaleUpdate(new StdCycle().setLenght(max_age).setMin(scale / 1000F).setMax(scale / 100F)));
		return result;
	}
	
	@Override
	public void onUnequipped(ItemStack item, EntityLivingBase living) {
		TimeSnapshot snapshot = living.getCapability(AlchemyCapabilityLoader.time_leap, null);
		if (snapshot != null)
			snapshot.list.clear();
	}
	
	@Override
	public void onWornTick(ItemStack item, EntityLivingBase living) {
		TimeSnapshot snapshot = living.getCapability(AlchemyCapabilityLoader.time_leap, null);
		if (snapshot != null)
			snapshot.updateTimeNode(living);
		if (Always.isServer() && living.ticksExisted % REPAIR_INTERVAL == 0) {
			IItemHandler handler = living.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
			for (int i = 0, len = handler.getSlots(); i < len; i++) {
				ItemStack old = handler.getStackInSlot(i);
				if (old != null && old.isItemDamaged() && Enchantments.MENDING.canApply(old))
					old.setItemDamage(old.getItemDamage() - 1);
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public KeyBinding[] getKeyBindings() {
		AlchemyModLoader.checkState();
		return new KeyBinding[] {
				new AlchemyKeyBinding(KEY_DESCRIPTION, Keyboard.KEY_V)
		};
	}
	
	@SideOnly(Side.CLIENT)
	@KeyEvent(KEY_DESCRIPTION)
	public void onKeyTimeLeapPressed(KeyBinding binding) {
		if (isCDOver()) {
			AlchemyNetworkHandler.network_wrapper.sendToServer(new MessageTimeLeap());
			Minecraft.getMinecraft().thePlayer.getEntityData().setInteger(NBT_KEY_CD, Minecraft.getMinecraft().thePlayer.ticksExisted);
			timeLeapOnClinet(Minecraft.getMinecraft().thePlayer);
		}
	}
	
	public static class MessageTimeLeap implements IMessage {
		
		@Override
		public void fromBytes(ByteBuf buf) { }

		@Override
		public void toBytes(ByteBuf buf) { }
		
	}
	
	@Override
	public Class<MessageTimeLeap> getServerMessageClass() {
		return MessageTimeLeap.class;
	}
	
	@Override
	public IMessage onMessage(MessageTimeLeap message, MessageContext ctx) {
		timeLeapOnServer(ctx.getServerHandler().playerEntity);
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	public void timeLeapOnClinet(final EntityPlayer player) {
		final TimeSnapshot snapshot = player.getCapability(AlchemyCapabilityLoader.time_leap, null);
		if (snapshot.isUpdate()) {
			snapshot.setUpdate(false);
			AlchemyEventSystem.addInputHook(this);
			AlchemyEventSystem.addDelayedRunnable(new IPhaseRunnable() {
				@Override
				public void run(Phase phase) {
					if (Always.isPlaying())
						player.addPotionEffect(new PotionEffect(MobEffects.SPEED, TimeSnapshot.SIZE / 2, 3));
				}
			}, 0);
			final Iterator<TimeNode> iterator = snapshot.list.iterator();
			AlchemyEventSystem.addContinuedRunnable(new IContinuedRunnable() {
				@Override
				public boolean run(Phase phase) {
					if (Always.isPlaying()) {
						if (iterator.hasNext())
							iterator.next().updateEntityOnClient(player);
						if (!iterator.hasNext()) {
							snapshot.setUpdate(true);
							AlchemyEventSystem.removeInputHook(ItemRingTime.this);
							return true;
						}
					} else 
						return true;
					return false;
				}
			});
		}
	}
	
	public void timeLeapOnServer(final EntityPlayer player) {	
		final TimeSnapshot snapshot = player.getCapability(AlchemyCapabilityLoader.time_leap, null);
		if (isEquipmented(player) && snapshot.isUpdate()) {
			snapshot.setUpdate(false);
			AlchemyEventSystem.addDelayedRunnable(new IPhaseRunnable() {
				@Override
				public void run(Phase phase) {
					player.addPotionEffect(new PotionEffect(MobEffects.SPEED, TimeSnapshot.SIZE / 2, 3));
					player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, TimeSnapshot.SIZE / 2, 3));
				}
			}, 0);
			final Iterator<TimeNode> iterator = snapshot.list.iterator();
			AlchemyEventSystem.addContinuedRunnable(new IContinuedRunnable() {
				@Override
				public boolean run(Phase phase) {
					List<Double6IntArrayPackage> d6iaps = new LinkedList<Double6IntArrayPackage>();
					int update[] = FXUpdateHelper.getIntArrayByArgs(FX_KEY_GATHER, 240, 200);
					for (int i = 0; i < 3; i++)
						d6iaps.add(new Double6IntArrayPackage(
								player.posX + 6 - player.worldObj.rand.nextFloat() * 12,
								player.posY + 6 - player.worldObj.rand.nextFloat() * 12,
								player.posZ + 6 - player.worldObj.rand.nextFloat() * 12, 0, 0, 0, update));
					AlchemyNetworkHandler.spawnParticle(FXWisp.Info.type,
							AABBHelper.getAABBFromEntity(player, AlchemyNetworkHandler.getParticleRange()), player.worldObj, d6iaps);
					boolean result = false;
					if (iterator.hasNext())
						iterator.next().updateEntityOnServer(player);
					if (!iterator.hasNext()) {
						snapshot.setUpdate(true);
						return true;
					}
					return false;
				}
			});
		}
	}
	
	@Override
	public int getMaxCD() {
		return USE_CD;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getResidualCD() {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		return isEquipmented(player) ? 
				max(0, getMaxCD() - (player.ticksExisted - player.getEntityData().getInteger(NBT_KEY_CD))) : -1;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isCDOver() {
		return getResidualCD() == 0;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void setResidualCD(int cd) {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		player.getEntityData().setInteger(NBT_KEY_CD, player.ticksExisted - (getMaxCD() - cd));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void restartCD() {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		player.getEntityData().setInteger(NBT_KEY_CD, player.ticksExisted);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderID() {
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderCD(int x, int y, int w, int h) {}
	
	public ItemRingTime() {
		super("ring_time", 0xFFBA31);
	}

}
