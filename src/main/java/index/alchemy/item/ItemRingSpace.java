package index.alchemy.item;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.BlockChest;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import index.alchemy.client.AlchemyKeyBindLoader;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.ClientProxy;
import index.alchemy.core.Constants;
import index.alchemy.core.EventType;
import index.alchemy.core.IEventHandle;
import index.alchemy.gui.GUIID;
import index.alchemy.item.AlchemyItemBauble.AlchemyItemRing;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.network.MessageOpenGui;
import index.alchemy.network.MessageParticle;
import index.alchemy.network.MessageSpaceRingPickUp;
import index.alchemy.network.SDouble6Packect;
import index.alchemy.util.Cache;

public class ItemRingSpace extends AlchemyItemRing implements IItemInventory, IEventHandle {
	
	public static final int PICKUP_CD = 20 * 3;
	
	protected int size;
	
	@Override
	public ItemInventory getItemInventory(EntityPlayer player, ItemStack item) {
		if (item == null)
			return null;
		return new ItemInventory(player, item, size, 
				I18n.translateToLocal("inventory." + getUnlocalizedName().substring(Constants.ITEM)));
	}
	
	@Override
	public EventType[] getEventType() {
		return AlchemyEventSystem.EVENT_BUS;
	}
	
	public ItemRingSpace() {
		this("ring_space", 9 * 6);
	}
	
	public ItemRingSpace(String name, int size) {
		super(name, 0x6600CC);
		this.size = size;
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void handleKeyInput(KeyInputEvent event) {
		if (AlchemyKeyBindLoader.key_space_ring.isPressed()) {
			if (AlchemyItemLoader.ring_space.isEquipmented(Minecraft.getMinecraft().thePlayer))
				AlchemyNetworkHandler.networkWrapper.sendToServer(new MessageOpenGui(GUIID.SPACE_RING));
		} else if (AlchemyKeyBindLoader.key_space_ring_pickup.isPressed()) {
			if (AlchemyItemLoader.ring_space.isEquipmented(Minecraft.getMinecraft().thePlayer) &&
					ClientProxy.ring_space_pickup_last_time - Minecraft.getMinecraft().theWorld.getWorldTime() > PICKUP_CD) {
				AlchemyNetworkHandler.networkWrapper.sendToServer(new MessageSpaceRingPickUp());
				ClientProxy.ring_space_pickup_last_time = Minecraft.getMinecraft().theWorld.getWorldTime();
			}
		}
	}
	
	public void pickUp(EntityPlayer player) {
		ItemStack content = getFormPlayer(player);
		if (content == null)
			return;
		ItemInventory inventory = getItemInventory(player, content);
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
					new AxisAlignedBB(player.posX - 16, player.posY - 64, player.posZ - 16,
							player.posX + 16, player.posY + 64, player.posZ + 16)))
				AlchemyNetworkHandler.networkWrapper.sendTo(new MessageParticle(EnumParticleTypes.PORTAL.getParticleID(),
						d6p.toArray(new SDouble6Packect[d6p.size()])), (EntityPlayerMP) mp);
		}
	}

}