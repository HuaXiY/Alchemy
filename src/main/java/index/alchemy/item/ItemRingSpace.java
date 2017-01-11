package index.alchemy.item;

import java.util.LinkedList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import index.alchemy.api.ICoolDown;
import index.alchemy.api.IEventHandle;
import index.alchemy.api.IGuiHandle;
import index.alchemy.api.IInputHandle;
import index.alchemy.api.IInventoryProvider;
import index.alchemy.api.INetworkMessage;
import index.alchemy.api.annotation.KeyEvent;
import index.alchemy.capability.AlchemyCapabilityLoader;
import index.alchemy.client.AlchemyKeyBinding;
import index.alchemy.client.render.HUDManager;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.interacting.ModItems;
import index.alchemy.inventory.AlchemyInventory;
import index.alchemy.inventory.InventoryItem;
import index.alchemy.item.AlchemyItemBauble.AlchemyItemRing;
import index.alchemy.item.ItemRingSpace.MessageSpaceRingPickup;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.network.Double6IntArrayPackage;
import index.alchemy.util.AABBHelper;
import index.alchemy.util.Always;
import index.project.version.annotation.Omega;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static java.lang.Math.*;

@Omega
public class ItemRingSpace extends AlchemyItemRing implements IInventoryProvider<ItemStack>, IInputHandle, IGuiHandle,
	IEventHandle, ICoolDown, INetworkMessage.Server<MessageSpaceRingPickup> {
	
	public static final int PICKUP_CD = 20 * 3, SIZE = 9 * 6;
	public static final String NBT_KEY_CD = "cd_ring_space", KEY_DESCRIPTION_OPEN = "key.space_ring_open";
	
	public static final ItemRingSpace type = null;
	
	@Override
	public InventoryItem initInventory() {
		return new InventoryItem(SIZE, I18n.translateToLocal(getInventoryUnlocalizedName()));
	}
	
	@Override
	public InventoryItem getInventory(ItemStack item) {
		return item == null ? null : (InventoryItem) item.getCapability(AlchemyCapabilityLoader.inventory, null);
	}
	
	@Override
	public String getInventoryUnlocalizedName() {
		return getUnlocalizedName().replace("item.", "inventory.");
	}
	
	@Override
	public void onWornTick(ItemStack item, EntityLivingBase living) {
		if (Always.isServer())
			if (living instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer) living;
				if (player.getHealth() > 0.0F && !player.isSpectator())
					for (EntityItem entity : player.worldObj.getEntitiesWithinAABB(EntityItem.class,
							player.getEntityBoundingBox().expand(5D, 5D, 5D)))
						if (!entity.isDead)
							entity.onCollideWithPlayer(player);
			}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEnderTeleport(EnderTeleportEvent event) {
		EntityLivingBase target = event.getEntityLiving();
		for (EntityLivingBase living : target.worldObj.getEntitiesWithinAABB(EntityLivingBase.class,
				AABBHelper.getAABBFromEntity(target, 5)))
			if (target != living && isEquipmented(living)) {
				event.setCanceled(true);
				return;
			}
		if (isEquipmented(target))
			event.setAttackDamage(0);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onLivingAttack(LivingAttackEvent event) {
		if (event.getSource() == DamageSource.outOfWorld && isEquipmented(event.getEntityLiving()))
			event.setCanceled(true);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public KeyBinding[] getKeyBindings() {
		AlchemyModLoader.checkState();
		return new KeyBinding[] {
				key_binding_1,
				key_binding_2,
				new AlchemyKeyBinding(KEY_DESCRIPTION_OPEN, Keyboard.KEY_R)
		};
	}
	
	@SideOnly(Side.CLIENT)
	@KeyEvent(KEY_DESCRIPTION_OPEN)
	public void onKeyOpenPressed(KeyBinding binding) {
		if (isEquipmented(Minecraft.getMinecraft().thePlayer))
			AlchemyNetworkHandler.openGui(this);
	}
	
	@SideOnly(Side.CLIENT)
	@KeyEvent({ KEY_RING_1, KEY_RING_2 })
	public void onKeyPickupPressed(KeyBinding binding) {
		if (shouldHandleInput(binding))
			if (isCDOver()) {
				AlchemyNetworkHandler.network_wrapper.sendToServer(new MessageSpaceRingPickup());
				restartCD();
			} else
				HUDManager.setSnake(this);
	}
	
	public static class MessageSpaceRingPickup implements IMessage, IMessageHandler<MessageSpaceRingPickup, IMessage> {
		
		@Override
		public void fromBytes(ByteBuf buf) { }

		@Override
		public void toBytes(ByteBuf buf) { }
		
		@Override
		public IMessage onMessage(MessageSpaceRingPickup message, MessageContext ctx) {
			AlchemyEventSystem.addDelayedRunnable(p -> type.pickup(ctx.getServerHandler().playerEntity), 0);
			return null;
		}
		
	}
	
	@Override
	public Class<MessageSpaceRingPickup> getServerMessageClass() {
		return MessageSpaceRingPickup.class;
	}
	
	public void pickup(EntityPlayer player) {
		AlchemyInventory inventory = getInventory(getFormLiving(player));
		if (inventory == null)
			return;
		List<EntityItem> list = player.worldObj.getEntitiesWithinAABB(EntityItem.class, AABBHelper.getAABBFromEntity(player, 8D));
		List<Double6IntArrayPackage> d6iaps = new LinkedList<Double6IntArrayPackage>(); 
		for (EntityItem entity : list) {
			inventory.mergeItemStack(entity.getEntityItem());
			if (entity.getEntityItem().stackSize < 1) {
				for (int i = 0; i < 4; i++)
					d6iaps.add(new Double6IntArrayPackage(
							entity.posX + entity.rand.nextGaussian() * .3,
							entity.posY + entity.rand.nextGaussian() * .3,
							entity.posZ + entity.rand.nextGaussian() * .3, 0, 0, 0));
				entity.setDead();
			}
		}
		if (list.size() > 0) {
			AlchemyNetworkHandler.spawnParticle(EnumParticleTypes.PORTAL, AABBHelper.getAABBFromEntity(player,
					AlchemyNetworkHandler.getParticleRange()), player.worldObj, d6iaps);
		}
	}
	
	@Override
	public Object getServerGuiElement(EntityPlayer player, World world, int x, int y, int z) {
		InventoryItem inventory = getInventory(getFormLiving(player));
		return inventory == null ? null : new ContainerChest(player.inventory, inventory, player);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object getClientGuiElement(EntityPlayer player, World world, int x, int y, int z) {
		InventoryItem inventory = getInventory(getFormLiving(player));
		return inventory == null ? null : new GuiChest(player.inventory, inventory);
	}
	
	@Override
	public int getMaxCD() {
		return PICKUP_CD;
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
		return 1;
	}

	public ItemRingSpace() {
		super("ring_space", 0xE451F2);
		alchemyMaterials.addAll(Always.generateMaterialConsumers("ingotSilver", 2, Items.MAGMA_CREAM,
				ModItems.bop$flower_burning_blossom));
	}

}