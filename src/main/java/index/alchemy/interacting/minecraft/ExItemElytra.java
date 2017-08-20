package index.alchemy.interacting.minecraft;

import org.lwjgl.input.Keyboard;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import index.alchemy.api.IBaubleEquipment;
import index.alchemy.api.annotation.Hook;
import index.alchemy.api.annotation.Hook.Type;
import index.alchemy.api.annotation.Patch;
import index.alchemy.capability.AlchemyCapabilityLoader;
import index.alchemy.inventory.InventoryBauble;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.client.renderer.entity.layers.LayerCape;
import net.minecraft.client.renderer.entity.layers.LayerElytra;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Patch("net.minecraft.item.ItemElytra")
public class ExItemElytra extends ItemElytra implements IBauble, IBaubleEquipment {

	@Override
	public BaubleType getBaubleType(ItemStack itemstack) {
		return BaubleType.BODY;
	}
	
	@Override
	public boolean canEquip(ItemStack itemstack, EntityLivingBase living) {
		return !isEquipmented(living);
	}
	
	@Override
	public ItemStack getFormLiving(EntityLivingBase living) {
		return getFormLiving0(living);
	}
	
	public static ItemStack getFormLiving0(EntityLivingBase living) {
		ItemStack item = living.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
		if (item.getItem() == Items.ELYTRA)
			return item;
		InventoryBauble bauble = living.getCapability(AlchemyCapabilityLoader.bauble, null);
		if (bauble == null)
			return ItemStack.EMPTY;
		return bauble.getItemStackFromSlot(BaubleType.BODY);
	}
	
	// !!!> ClassLoader will throw ClassCircularityError when this logic use hook <!!!
	@Patch("net.minecraft.entity.EntityLivingBase")
	public static abstract class Patch$EntityLivingBase extends EntityLivingBase {
		
		// No use, just to compile
		@Patch.Exception
		private Patch$EntityLivingBase(World worldIn) {
			super(worldIn);
		}
		
		@SuppressWarnings("unused")
		private void updateElytra() {
			boolean flag = getFlag(7);
			if (flag && !onGround && !isRiding()) {
				ItemStack item = getFormLiving0(this);
				if (item.getItem() == Items.ELYTRA && ItemElytra.isUsable(item)) {
					flag = true;
					if (!world.isRemote && (ticksElytraFlying + 1) % 20 == 0)
						item.damageItem(1, this);
				} else
					flag = false;
			} else
				flag = false;
			if (!world.isRemote)
				setFlag(7, flag);
		}
		
	}
	
	@Hook.Provider
	public static class HookServer {
		
		@Hook("net.minecraft.network.NetHandlerPlayServer#func_147357_a")
		public static Hook.Result processEntityAction(NetHandlerPlayServer handler, CPacketEntityAction action) {
			if (action.getAction() == Action.START_FALL_FLYING) {
				PacketThreadUtil.checkThreadAndEnqueue(action, handler, handler.player.getServerWorld());
				handler.player.markPlayerActive();
				if (!handler.player.onGround && handler.player.motionY < 0.0D &&
						!handler.player.isElytraFlying() && !handler.player.isInWater()) {
					ItemStack item = getFormLiving0(handler.player);
					if (item.getItem() == Items.ELYTRA && ItemElytra.isUsable(item))
						handler.player.setElytraFlying();
				} else
					handler.player.clearElytraFlying();
				return Hook.Result.NULL;
			}
			return Hook.Result.VOID;
		}
		
	}
	
	@Hook.Provider
	@SideOnly(Side.CLIENT)
	public static class HookClient {
		
		private static boolean flag;
		
		@Hook(value = "net.minecraft.client.entity.EntityPlayerSP#func_70636_d", type = Type.TAIL)
		public static void onLivingUpdate_tail(EntityPlayerSP player) {
			ItemStack item = getFormLiving0(player);
			KeyBinding jump = Minecraft.getMinecraft().gameSettings.keyBindJump;
			boolean flag = Keyboard.isKeyDown(jump.getKeyCode());
			if (item.getItem() == Items.ELYTRA) {
				if (flag && jump.getKeyConflictContext().isActive() && !HookClient.flag &&
						player.motionY < 0.0D && !player.isElytraFlying() && !player.capabilities.isFlying)
					if (ItemElytra.isUsable(item))
						player.connection.sendPacket(new CPacketEntityAction(player, CPacketEntityAction.Action.START_FALL_FLYING));
			}
			HookClient.flag = flag;
		}
		
	}
	
	@Hook.Provider
	@SideOnly(Side.CLIENT)
	public static class HookRender {
		
		@Hook("net.minecraft.client.renderer.entity.layers.LayerCape#func_177141_a")
		public static Hook.Result doRenderLayer_cape(LayerCape layer, AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
				float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
			return getFormLiving0(player).getItem() == Items.ELYTRA ? Hook.Result.NULL : Hook.Result.VOID;
		}
		
		@Hook("net.minecraft.client.renderer.entity.layers.LayerElytra#func_177141_a")
		public static Hook.Result doRenderLayer_elytra(LayerElytra layer, EntityLivingBase living, float limbSwing, float limbSwingAmount,
				float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
			ItemStack item = getFormLiving0(living);
			if (item.getItem() == Items.ELYTRA) {
				{
					GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		            GlStateManager.enableBlend();
		            GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

		            if (living instanceof AbstractClientPlayer) {
		                AbstractClientPlayer player = (AbstractClientPlayer) living;
		                if (player.isPlayerInfoSet() && player.getLocationElytra() != null)
		                	layer.renderPlayer.bindTexture(player.getLocationElytra());
		                else if (player.hasPlayerInfo() && player.getLocationCape() != null && player.isWearing(EnumPlayerModelParts.CAPE))
		                	layer.renderPlayer.bindTexture(player.getLocationCape());
		                else
		                	layer.renderPlayer.bindTexture(LayerElytra.TEXTURE_ELYTRA);
		            } else
		            	layer.renderPlayer.bindTexture(LayerElytra.TEXTURE_ELYTRA);

		            GlStateManager.pushMatrix();
		            GlStateManager.translate(0.0F, 0.0F, 0.125F);
		            layer.modelElytra.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, living);
		            layer.modelElytra.render(living, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

		            if (item.isItemEnchanted())
						LayerArmorBase.renderEnchantedGlint(layer.renderPlayer, living, layer.modelElytra, limbSwing, limbSwingAmount,
								partialTicks, ageInTicks, netHeadYaw, headPitch, scale);

		            GlStateManager.disableBlend();
		            GlStateManager.popMatrix();
				}
				return Hook.Result.NULL;
			}
			return Hook.Result.VOID;
		}
		
	}

}
