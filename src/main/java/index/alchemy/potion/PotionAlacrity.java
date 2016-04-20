package index.alchemy.potion;

import org.lwjgl.input.Keyboard;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import index.alchemy.client.ClientProxy;
import index.alchemy.core.IPlayerTickable;
import index.alchemy.entity.AlchemyDamageSourceLoader;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.network.MessageAlacrityCallback;

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