package index.alchemy.enchantment;

import org.lwjgl.input.Keyboard;

import index.alchemy.api.IPlayerTickable;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EnchantmentDoubleJump extends AlchemyEnchantment implements IPlayerTickable {
	
	public static final String NBT_KEY_CD = "ench_double_jump";
	public static final int JUMP_INTERVAL = 300;
	public static final double JUMP_MOTION = 0.5D;
	
	@SideOnly(Side.CLIENT)
	public static long last_on_ground_time;
	
	@Override
	public Side getSide() {
		return Side.CLIENT;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onTick(EntityPlayer player, Phase phase) {
		if (phase == Phase.START && Minecraft.getMinecraft().thePlayer == player)
			if (player.onGround) {
				player.getEntityData().setBoolean(NBT_KEY_CD, true);
				last_on_ground_time = System.currentTimeMillis();
			} else if (Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindJump.getKeyCode())
					&& System.currentTimeMillis() - last_on_ground_time > JUMP_INTERVAL
					&& EnchantmentHelper.getEnchantmentLevel(this, player.inventory.armorInventory[0]) > 0
					&& player.getEntityData().getBoolean(NBT_KEY_CD)) {
					player.motionY = JUMP_MOTION;
					player.getEntityData().setBoolean(NBT_KEY_CD, false);
			}
	}
	
	public EnchantmentDoubleJump() {
		super("double_jump", Rarity.RARE, EnumEnchantmentType.ARMOR_FEET, 1, EntityEquipmentSlot.FEET);
	}

}
