package index.alchemy.potion;

import java.util.Random;

import index.alchemy.api.IRegister;
import index.alchemy.client.render.HUDManager;
import index.alchemy.core.AlchemyInitHook;
import index.alchemy.core.AlchemyResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AlchemyPotion extends Potion implements IRegister {
	
	protected static final Random RANDOM = new Random();
	
	public static final ResourceLocation RESOURCE_LOCATION = new AlchemyResourceLocation("potion");
	
	private static int current_id = -1;
	
	private boolean ready;
	private int id;
	
	@Override
	public boolean isReady(int tick, int level) {
		return ready;
	}
	
	@Override
	public boolean isInstant() {
		return ready;
	}
	
	/*@Override
	@SideOnly(Side.CLIENT)
	public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
		mc.getTextureManager().bindTexture(RESOURCE_LOCATION);
		mc.currentScreen.drawTexturedModalRect(x + 6, y + 6, id % 16 * 16, id / 16, 16, 16);
	}*/
	@Override
	@SideOnly(Side.CLIENT)
	public int getStatusIconIndex() {
		HUDManager.bind(RESOURCE_LOCATION);
		return id;
	}
	
	@Override
	public void affectEntity(Entity source, Entity indirect, EntityLivingBase living, int level, double health) {
		performEffect(living, level);
	}
	
	public AlchemyPotion(String name, boolean isbad, int color) {
		this(name, isbad, color, false);
	}
	
	public AlchemyPotion(String name, boolean isbad, int color, boolean ready) {
		super(isbad, color);
		this.ready = ready;
		id = ++current_id;
		setPotionName("effect." + name);
		setRegistryName("potion_" + name);
		register();
	}
	
	@Override
	public void register() {
		AlchemyInitHook.init_impl(this);
	}
	
}
