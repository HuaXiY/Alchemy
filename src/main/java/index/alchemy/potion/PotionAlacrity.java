package index.alchemy.potion;

import org.lwjgl.input.Keyboard;

import index.alchemy.client.ClientProxy;
import index.alchemy.core.IPlayerTickable;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.network.MessageAlacrityCallback;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

public class PotionAlacrity extends AlchemyPotion implements IPlayerTickable {
	
	@Override
	public Side getSide() {
		return Side.CLIENT;
	}
	
	@Override
	public void onTick(EntityPlayer player) {
		double v = 1.8, vxz = 4.2;
		if (--ClientProxy.potion_alacrity_cd <= 0 && player.isPotionActive(this) && player.motionY < 0 &&
				Keyboard.isKeyDown(ClientProxy.minecraft.gameSettings.keyBindJump.getKeyCode())) {
			player.motionY += player.motionX == 0 && player.motionZ == 0 ? v * 1.2 : v;
			player.motionX *= vxz;
			player.motionZ *= vxz;
			ClientProxy.potion_alacrity_cd = 40;
			AlchemyNetworkHandler.networkWrapper.sendToServer(new MessageAlacrityCallback());
		}
	}
	
	public static void callback(EntityPlayer player) {
		if (player.isPotionActive(AlchemyPotionLoader.alacrity))
			player.fallDistance = 0;
	}
	
	public PotionAlacrity() {
		super("alacrity", false, 0x66CCFF);
	}

}