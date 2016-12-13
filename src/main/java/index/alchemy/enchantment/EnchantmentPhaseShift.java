package index.alchemy.enchantment;

import org.lwjgl.input.Keyboard;

import index.alchemy.api.ICoolDown;
import index.alchemy.api.IInputHandle;
import index.alchemy.api.annotation.KeyEvent;
import index.alchemy.client.render.HUDManager;
import index.alchemy.core.AlchemyModLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static org.lwjgl.opengl.GL11.*;
import static net.minecraft.util.math.MathHelper.*;
import static java.lang.Math.*;

public class EnchantmentPhaseShift extends AlchemyEnchantment implements IInputHandle, ICoolDown {
	
	public static final String NBT_KEY_CD = "cd_phase_shift", NBT_KEY_LAST = "last_phase_shift",
			KEY_LEFT = "key.left", KEY_RIGHT = "key.right", KEY_UP = "key.forward", KEY_DOWN = "key.back";
	public static final int PHASE_SHIFT_CD = 20 * 4, PHASE_SHIFT_CD_NUM = 3, PHASE_SHIFT_INTERVAL = 500, FONT_SIZE = 2;
	public static final double BALANCE_COEFFICIENT = 0.5D, PHASE_SHIFT_MOTION = 3;
	
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
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		if (Keyboard.isKeyDown(settings.keyBindSprint.getKeyCode())
				&& System.currentTimeMillis() - player.getEntityData().getLong(NBT_KEY_LAST) > PHASE_SHIFT_INTERVAL) {
			if (isCDOver()) {
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
					strafe *= BALANCE_COEFFICIENT;
					forward *= BALANCE_COEFFICIENT;
				}
				float sin = sin(player.rotationYaw * 0.017453292F);
	            float cos = cos(player.rotationYaw * 0.017453292F);
	            player.motionX += strafe * cos - forward * sin;
	            player.motionZ += forward * cos + strafe * sin;
				restartCD();
			} else
				HUDManager.setSnake(this);
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
		if (EnchantmentHelper.getEnchantmentLevel(this, player.inventory.armorInventory[0]) > 0) {
			int cd = max(0, getMaxCD() - (player.ticksExisted - player.getEntityData().getInteger(NBT_KEY_CD)));
			return max(ceil((double) cd / PHASE_SHIFT_CD) == 0 ? 0 : 1, cd % PHASE_SHIFT_CD);
		} else
			return -1;
	}
	
	@SideOnly(Side.CLIENT)
	public int getNumber() {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		if (EnchantmentHelper.getEnchantmentLevel(this, player.inventory.armorInventory[0]) <= 0)
			return -1;
		return PHASE_SHIFT_CD_NUM - (int) ceil((double) max(0,
				getMaxCD() - (player.ticksExisted - player.getEntityData().getInteger(NBT_KEY_CD))) / PHASE_SHIFT_CD);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isCDOver() {
		return getNumber() > 0;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void setResidualCD(int cd) {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		player.getEntityData().setInteger(NBT_KEY_CD, cd);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void restartCD() {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		player.getEntityData().setInteger(NBT_KEY_CD, getNumber() == PHASE_SHIFT_CD_NUM ? player.ticksExisted :
			player.getEntityData().getInteger(NBT_KEY_CD) + getMaxCD());
		player.getEntityData().setLong(NBT_KEY_LAST, System.currentTimeMillis());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderID() {
		return 5;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderCD(int x, int y, int w, int h) {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		int num = getNumber();
		GuiIngame gui = Minecraft.getMinecraft().ingameGUI;
		FontRenderer renderer = Minecraft.getMinecraft().fontRendererObj;
		String text = String.valueOf(num);
		glScalef(FONT_SIZE, FONT_SIZE, 1F);
		gui.drawString(renderer, text, (x + w - renderer.getStringWidth(text) * FONT_SIZE - 3) / FONT_SIZE,
				(y + h - renderer.FONT_HEIGHT * FONT_SIZE - 3) / FONT_SIZE, 0xFF000000);
		glScalef(1F / FONT_SIZE, 1F / FONT_SIZE, 1F);
	}
	
	public EnchantmentPhaseShift() {
		super("phase_shift", Rarity.VERY_RARE, EnumEnchantmentType.ARMOR_FEET, 1, EntityEquipmentSlot.FEET);
	}

}
