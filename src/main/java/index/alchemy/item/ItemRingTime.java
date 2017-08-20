package index.alchemy.item;

import static java.lang.Math.max;

import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;

import index.alchemy.animation.StdCycle;
import index.alchemy.api.ICoolDown;
import index.alchemy.api.IFXUpdate;
import index.alchemy.api.IInputHandle;
import index.alchemy.api.INetworkMessage;
import index.alchemy.api.annotation.FX;
import index.alchemy.api.annotation.KeyEvent;
import index.alchemy.capability.AlchemyCapabilityLoader;
import index.alchemy.capability.CapabilityTimeLeap.TimeSnapshot;
import index.alchemy.capability.CapabilityTimeLeap.TimeSnapshot.TimeNode;
import index.alchemy.client.color.ColorHelper;
import index.alchemy.client.fx.FXWisp;
import index.alchemy.client.fx.update.FXARGBIteratorUpdate;
import index.alchemy.client.fx.update.FXAgeUpdate;
import index.alchemy.client.fx.update.FXBrightnessUpdate;
import index.alchemy.client.fx.update.FXScaleUpdate;
import index.alchemy.client.fx.update.FXUpdateHelper;
import index.alchemy.client.render.HUDManager;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.item.AlchemyItemBauble.AlchemyItemRing;
import index.alchemy.item.ItemRingTime.MessageTimeLeap;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.network.Double6IntArrayPackage;
import index.alchemy.util.AABBHelper;
import index.alchemy.util.Always;
import index.alchemy.util.Tool;
import index.project.version.annotation.Omega;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

@Omega
@FX.UpdateProvider
public class ItemRingTime extends AlchemyItemRing implements IInputHandle, INetworkMessage.Server<MessageTimeLeap>, ICoolDown {
	
	public static final int USE_CD = 20 * 18, REPAIR_INTERVAL = 20 * 10;
	public static final String NBT_KEY_CD = "cd_ring_time", FX_KEY_GATHER = "ring_time_gather";
	
	public static final ItemStack mending_book = Always.getEnchantmentBook(Enchantments.MENDING);
	
	public static final ItemRingTime type = null;
	
	@FX.UpdateMethod(FX_KEY_GATHER)
	public static List<IFXUpdate> getFXUpdateGather(int[] args) {
		List<IFXUpdate> result = Lists.newLinkedList();
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
			snapshot.clear();
	}
	
	@Override
	public void onWornTick(ItemStack item, EntityLivingBase living) {
		TimeSnapshot snapshot = living.getCapability(AlchemyCapabilityLoader.time_leap, null);
		if (snapshot != null)
			snapshot.updateTimeNode(living);
		if (Always.isServer() && living.ticksExisted % REPAIR_INTERVAL == 0) {
			IItemHandler handler = living.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.NORTH);
			for (int i = 0, len = handler.getSlots(); i < len; i++)
				itemToTimeBack(handler.getStackInSlot(i));
			itemToTimeBack(living.getHeldItemMainhand());
		}
	}
	
	public void itemToTimeBack(ItemStack old) {
		if (old != null && old.isItemDamaged() && old.getItem().isBookEnchantable(old, mending_book))
			old.setItemDamage(old.getItemDamage() - 1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public KeyBinding[] getKeyBindings() {
		AlchemyModLoader.checkState();
		return new KeyBinding[] {
				key_binding_1,
				key_binding_2
		};
	}
	
	@SideOnly(Side.CLIENT)
	@KeyEvent({ KEY_RING_1, KEY_RING_2 })
	public void onKeyTimeLeapPressed(KeyBinding binding) {
		if (shouldHandleInput(binding))
			if (isCDOver()) {
				AlchemyNetworkHandler.network_wrapper.sendToServer(new MessageTimeLeap());
				restartCD();
				timeLeapOnClinet(Minecraft.getMinecraft().player);
			} else
				HUDManager.setSnake(this);
	}
	
	public static class MessageTimeLeap implements IMessage, IMessageHandler<MessageTimeLeap, IMessage> {
		
		@Override
		public void fromBytes(ByteBuf buf) { }

		@Override
		public void toBytes(ByteBuf buf) { }
		
		@Override
		public IMessage onMessage(MessageTimeLeap message, MessageContext ctx) {
			type.timeLeapOnServer(ctx.getServerHandler().player);
			return null;
		}
		
	}
	
	@Override
	public Class<MessageTimeLeap> getServerMessageClass() {
		return MessageTimeLeap.class;
	}
	
	@SideOnly(Side.CLIENT)
	public void timeLeapOnClinet(EntityPlayer player) {
		TimeSnapshot snapshot = player.getCapability(AlchemyCapabilityLoader.time_leap, null);
		if (snapshot.isUpdate()) {
			snapshot.setUpdate(false);
			snapshot.setLeaping(true);
			AlchemyEventSystem.addInputHook(this);
			Iterator<TimeNode> iterator = snapshot.iterator();
			AlchemyEventSystem.addContinuedRunnable(p -> {
				if (Always.isPlaying()) {
					if (iterator.hasNext())
						iterator.next().updateEntityOnClient(player);
					if (!iterator.hasNext()) {
						snapshot.setUpdate(true);
						snapshot.setLeaping(false);
						AlchemyEventSystem.removeInputHook(ItemRingTime.this);
						return true;
					}
				} else 
					return true;
				return false;
			});
		}
	}
	
	public void timeLeapOnServer(EntityPlayer player) {	
		TimeSnapshot snapshot = player.getCapability(AlchemyCapabilityLoader.time_leap, null);
		if (isEquipmented(player) && snapshot.isUpdate()) {
			snapshot.setUpdate(false);
			snapshot.setLeaping(true);
			Iterator<TimeNode> iterator = snapshot.iterator();
			AlchemyEventSystem.addContinuedRunnable(p -> {
				List<Double6IntArrayPackage> d6iaps = new LinkedList<Double6IntArrayPackage>();
				int update[] = FXUpdateHelper.getIntArrayByArgs(FX_KEY_GATHER, 240, 200);
				for (int i = 0; i < 3; i++)
					d6iaps.add(new Double6IntArrayPackage(
							player.posX + player.world.rand.nextGaussian() * 6,
							player.posY + player.world.rand.nextGaussian() * 6,
							player.posZ + player.world.rand.nextGaussian() * 6, 0, 0, 0, update));
				AlchemyNetworkHandler.spawnParticle(FXWisp.Info.type,
						AABBHelper.getAABBFromEntity(player, AlchemyNetworkHandler.getParticleRange()), player.world, d6iaps);
				if (iterator.hasNext())
					iterator.next().updateEntityOnServer(player);
				if (!iterator.hasNext()) {
					snapshot.setUpdate(true);
					snapshot.setLeaping(false);
					return true;
				}
				return false;
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
		EntityPlayer player = Minecraft.getMinecraft().player;
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
		EntityPlayer player = Minecraft.getMinecraft().player;
		player.getEntityData().setInteger(NBT_KEY_CD, player.ticksExisted - (getMaxCD() - cd));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void restartCD() {
		EntityPlayer player = Minecraft.getMinecraft().player;
		player.getEntityData().setInteger(NBT_KEY_CD, player.ticksExisted);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderID() {
		return 0;
	}

	public ItemRingTime() {
		super("ring_time", 0xFFBA31);
	}

}
