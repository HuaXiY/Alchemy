package index.alchemy.potion;

import org.lwjgl.input.Keyboard;

import index.alchemy.api.ICoolDown;
import index.alchemy.api.INetworkMessage;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.potion.PotionAlacrity.MessageAlacrityCallback;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PotionAlacrity extends AlchemyPotion implements ICoolDown, INetworkMessage.Server<MessageAlacrityCallback> {
	
	public static final int JUMP_AIR_CD = 20 * 2;
	public static final String NBT_KEY_CD = "potion_alacrity";
	
	@Override
	@SideOnly(Side.CLIENT)
	public void performEffect(EntityLivingBase living, int level) {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		if (living == player) {
			double v = 1.8, vxz = 4.2;
			if (isCDOver() && player.isPotionActive(PotionAlacrity.this) && player.motionY < 0 &&
					Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindJump.getKeyCode())) {
				player.motionY += player.motionX == 0 && player.motionZ == 0 ? v * 1.2 : v;
				player.motionX *= vxz;
				player.motionZ *= vxz;
				AlchemyNetworkHandler.network_wrapper.sendToServer(new MessageAlacrityCallback());
				restartCD();
			}
		}
	}
	
	public static class MessageAlacrityCallback implements IMessage {
		@Override
		public void fromBytes(ByteBuf buf) {}

		@Override
		public void toBytes(ByteBuf buf) {}
	}
	
	@Override
	public Class<MessageAlacrityCallback> getServerMessageClass() {
		return MessageAlacrityCallback.class;
	}
	
	@Override
	public IMessage onMessage(MessageAlacrityCallback message, MessageContext ctx) {
		callback(ctx.getServerHandler().playerEntity);
		return null;
	}
	
	public void callback(EntityPlayer player) {
		if (player.isPotionActive(this))
			player.fallDistance = 0;
	}
	
	@Override
	public int getMaxCD() {
		return JUMP_AIR_CD;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getResidualCD() {
		return Minecraft.getMinecraft().thePlayer.isPotionActive(PotionAlacrity.this) ? 
				Math.max(0, getMaxCD() - (Minecraft.getMinecraft().thePlayer.ticksExisted - Minecraft.getMinecraft().thePlayer.getEntityData().getInteger(NBT_KEY_CD))) : 0;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isCDOver() {
		return getResidualCD() <= 0;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void setResidualCD(int cd) {
		Minecraft.getMinecraft().thePlayer.getEntityData().setInteger(NBT_KEY_CD, Minecraft.getMinecraft().thePlayer.ticksExisted - (getMaxCD() - cd));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void restartCD() {
		Minecraft.getMinecraft().thePlayer.getEntityData().setInteger(NBT_KEY_CD, Minecraft.getMinecraft().thePlayer.ticksExisted);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderID() {
		return -1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderCD(int x, int y, int w, int h) {}
	
	public PotionAlacrity() {
		super("alacrity", false, 0x66CCFF);
	}

}