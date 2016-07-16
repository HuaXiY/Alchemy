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

import static java.lang.Math.*;

public class PotionAlacrity extends AlchemyPotion implements ICoolDown, INetworkMessage.Server<MessageAlacrityCallback> {
	
	public static final int JUMP_AIR_CD = 20;
	public static final double JUMP_V = 1.8, JUMP_V_Y = 1.2, JUMP_V_XZ = 4.2;
	public static final String NBT_KEY_CD = "potion_alacrity";
	
	@Override
	@SideOnly(Side.CLIENT)
	public void performEffect(EntityLivingBase living, int level) {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		if (living == player) {
			if (isCDOver() && player.motionY < 0 &&
					Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindJump.getKeyCode())) {
				player.motionY += player.motionX == 0 && player.motionZ == 0 ? JUMP_V * JUMP_V_Y : JUMP_V;
				player.motionX *= JUMP_V_XZ;
				player.motionZ *= JUMP_V_XZ;
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
		if (player.isPotionActive(this)) {
			player.motionY += player.motionX == 0 && player.motionZ == 0 ? JUMP_V * JUMP_V_Y : JUMP_V;
			player.motionX *= JUMP_V_XZ;
			player.motionZ *= JUMP_V_XZ;
			player.fallDistance = 0;
		}
	}
	
	@Override
	public int getMaxCD() {
		return JUMP_AIR_CD;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getResidualCD() {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		return player.isPotionActive(PotionAlacrity.this) ? 
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
		return -1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderCD(int x, int y, int w, int h) {}
	
	public PotionAlacrity() {
		super("alacrity", false, 0x66CCFF);
	}

}