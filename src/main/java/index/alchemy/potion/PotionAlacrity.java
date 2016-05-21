package index.alchemy.potion;

import org.lwjgl.input.Keyboard;

import index.alchemy.api.INetworkMessage;
import index.alchemy.client.ClientProxy;
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

public class PotionAlacrity extends AlchemyPotion implements INetworkMessage.Server<MessageAlacrityCallback> {
	
	@Override
	@SideOnly(Side.CLIENT)
	public void performEffect(EntityLivingBase living, int level) {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		if (living == player) {
			double v = 1.8, vxz = 4.2;
			if (--ClientProxy.potion_alacrity_cd <= 0 && player.isPotionActive(PotionAlacrity.this) && player.motionY < 0 &&
					Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindJump.getKeyCode())) {
				player.motionY += player.motionX == 0 && player.motionZ == 0 ? v * 1.2 : v;
				player.motionX *= vxz;
				player.motionZ *= vxz;
				ClientProxy.potion_alacrity_cd = 40;
				AlchemyNetworkHandler.networkWrapper.sendToServer(new MessageAlacrityCallback());
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
	
	public PotionAlacrity() {
		super("alacrity", false, 0x66CCFF);
	}

}