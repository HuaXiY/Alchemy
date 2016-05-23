package index.alchemy.enchantment;

import org.lwjgl.input.Keyboard;

import index.alchemy.annotation.KeyEvent;
import index.alchemy.api.ICoolDown;
import index.alchemy.api.IInputHandle;
import index.alchemy.core.AlchemyModLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EnchantmentPhaseShift extends AlchemyEnchantment implements IInputHandle, ICoolDown {
	
	public static final String NBT_KEY_CD = "ench_phase_shift", KEY_LEFT = "key.left", KEY_RIGHT = "key.right", KEY_UP = "key.forward", KEY_DOWN = "key.back";
	public static final int MIN_INTERVAL = 0, MAX_INTERVAL = 300, PHASE_SHIFT_CD = 20 * 4, PHASE_SHIFT_MOTION = 3;
	
	@Override
	@SideOnly(Side.CLIENT)
	public KeyBinding[] getKeyBindings() {
		AlchemyModLoader.checkState();
		GameSettings settings = Minecraft.getMinecraft().gameSettings;
		return new KeyBinding []{
				settings.keyBindLeft,
				settings.keyBindRight,
				settings.keyBindForward,
				settings.keyBindBack,
		};
	}
	
	@SideOnly(Side.CLIENT)
	@KeyEvent({ KEY_UP, KEY_DOWN, KEY_LEFT, KEY_RIGHT })
	public void onKeyMove(KeyBinding binding) {
		int key_code = binding.getKeyCode();
		GameSettings settings = Minecraft.getMinecraft().gameSettings;
		if (Keyboard.isKeyDown(settings.keyBindSprint.getKeyCode()) && isCDOver()) {
			EntityPlayer player = Minecraft.getMinecraft().thePlayer;
			double strafe = 0, forward = 0;
			if (key_code == settings.keyBindLeft.getKeyCode())
				strafe = PHASE_SHIFT_MOTION;
			else if (key_code == settings.keyBindRight.getKeyCode())
				strafe = -PHASE_SHIFT_MOTION;
			else if (key_code == settings.keyBindForward.getKeyCode())
				forward = PHASE_SHIFT_MOTION;
			else if (key_code == settings.keyBindBack.getKeyCode())
				forward = -PHASE_SHIFT_MOTION;
			if (!player.onGround) {
				strafe *= 0.5D;
				forward *= 0.5D;
			}
			float sin = MathHelper.sin(player.rotationYaw * 0.017453292F);
            float cos = MathHelper.cos(player.rotationYaw * 0.017453292F);
            player.motionX += (double)(strafe * cos - forward * sin);
            player.motionZ += (double)(forward * cos + strafe * sin);
			restartCD();
		}
	}
	
	@Override
	public int getMaxCD() {
		return PHASE_SHIFT_CD;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getResidualCD() {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		return EnchantmentHelper.getEnchantmentLevel(this, player.inventory.armorInventory[0]) > 0 ? 
				Math.max(0, getMaxCD() - (player.ticksExisted - player.getEntityData().getInteger(NBT_KEY_CD))) : -1;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isCDOver() {
		return getResidualCD() == 0;
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
		return 5;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderCD(int x, int y, int w, int h) {}
	
	public EnchantmentPhaseShift() {
		super("phase_shift", Rarity.RARE, EnumEnchantmentType.ARMOR_FEET, 1, EntityEquipmentSlot.FEET);
	}

}
