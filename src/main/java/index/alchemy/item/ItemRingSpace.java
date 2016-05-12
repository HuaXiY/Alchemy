package index.alchemy.item;

import java.util.LinkedList;
import java.util.List;

import index.alchemy.client.AlchemyKeyBindingLoader;
import index.alchemy.client.ClientProxy;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.Constants;
import index.alchemy.core.EventType;
import index.alchemy.core.IEventHandle;
import index.alchemy.gui.GUIID;
import index.alchemy.item.AlchemyItemBauble.AlchemyItemRing;
import index.alchemy.item.ItemRingSpace.MessageSpaceRingPickup;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.network.INetworkMessage;
import index.alchemy.network.MessageOpenGui;
import index.alchemy.network.MessageParticle;
import index.alchemy.network.SDouble6Packect;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemRingSpace extends AlchemyItemRing implements IItemInventory, IEventHandle, INetworkMessage<MessageSpaceRingPickup> {
	
	public static final int PICKUP_CD = 20 * 3;
	
	protected int size;
	
	@Override
	public ItemInventory getItemInventory(EntityPlayer player, ItemStack item) {
		if (item == null)
			return null;
		return new ItemInventory(player, item, size, I18n.translateToLocal(getInventoryUnlocalizedName()));
	}
	
	@Override
	public String getInventoryUnlocalizedName() {
		return "inventory." + getUnlocalizedName().substring(Constants.ITEM);
	}
	
	@Override
	public EventType[] getEventType() {
		return AlchemyEventSystem.EVENT_BUS;
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void handleKeyInput(KeyInputEvent event) {
		if (AlchemyKeyBindingLoader.key_space_ring_open.isPressed()) {
			if (Minecraft.getMinecraft().currentScreen != null)
				Minecraft.getMinecraft().displayGuiScreen(null);
			else if (isEquipmented(Minecraft.getMinecraft().thePlayer))
				AlchemyNetworkHandler.networkWrapper.sendToServer(new MessageOpenGui(GUIID.SPACE_RING));
		} else if (AlchemyKeyBindingLoader.key_space_ring_pickup.isPressed()) {
			if (isEquipmented(Minecraft.getMinecraft().thePlayer) &&
					Minecraft.getMinecraft().theWorld.getWorldTime() - ClientProxy.ring_space_pickup_last_time > PICKUP_CD) {
				AlchemyNetworkHandler.networkWrapper.sendToServer(new MessageSpaceRingPickup());
				ClientProxy.ring_space_pickup_last_time = Minecraft.getMinecraft().theWorld.getWorldTime();
			}
		}
	}
	
	public static class MessageSpaceRingPickup implements IMessage {
		@Override
		public void fromBytes(ByteBuf buf) {}

		@Override
		public void toBytes(ByteBuf buf) {}
	}
	
	@Override
	public Class<MessageSpaceRingPickup> getMessageClass() {
		return MessageSpaceRingPickup.class;
	}
	
	@Override
	public Side getMessageSide() {
		return Side.SERVER;
	}
	
	@Override
	public IMessage onMessage(MessageSpaceRingPickup message, MessageContext ctx) {
		pickup(ctx.getServerHandler().playerEntity);
		return null;
	}
	
	public ItemRingSpace() {
		this("ring_space", 9 * 6);
	}
	
	public ItemRingSpace(String name, int size) {
		super(name, 0x6600CC);
		this.size = size;
	}
	
	public void pickup(EntityPlayer player) {
		ItemInventory inventory = getItemInventory(player, getFormPlayer(player));
		if (inventory == null)
			return;
		List<EntityItem> list = player.worldObj.getEntitiesWithinAABB(EntityItem.class, 
				new AxisAlignedBB(player.posX - 5D, player.posY - 5D, player.posZ - 5D,
						player.posX + 5D, player.posY + 5D, player.posZ + 5D));
		List<SDouble6Packect> d6p = new LinkedList<SDouble6Packect>(); 
		for (EntityItem entity : list) {
			inventory.mergeItemStack(entity.getEntityItem());
			if (entity.getEntityItem().stackSize < 1) {
				for (int i = 0; i < 3; i++)
					d6p.add(new SDouble6Packect(entity.posX, entity.posY, entity.posZ, 1, 1, 1));
				entity.setDead();
			}
		}
		if (list.size() > 0) {
			inventory.updateNBT();
			for (EntityPlayerMP mp : player.worldObj.getEntitiesWithinAABB(EntityPlayerMP.class,
					new AxisAlignedBB(player.posX - 16D, player.posY - 64D, player.posZ - 16D,
							player.posX + 16D, player.posY + 64D, player.posZ + 16D)))
				AlchemyNetworkHandler.networkWrapper.sendTo(new MessageParticle(EnumParticleTypes.PORTAL.getParticleID(),
						d6p.toArray(new SDouble6Packect[d6p.size()])), (EntityPlayerMP) mp);
		}
	}

}