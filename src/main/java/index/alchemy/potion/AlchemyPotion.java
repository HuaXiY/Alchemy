package index.alchemy.potion;

import java.util.Random;

import index.alchemy.api.IRegister;
import index.alchemy.client.render.HUDManager;
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
	
	public static final int NOT_FLASHING_TIME = 20 * 12;
	
	private static int current_id = -1;
	
	private boolean instant;
	private int id;
	
	@Override
	public boolean isReady(int tick, int level) {
		return true;
	}
	
	@Override
	public boolean isInstant() {
		return instant;
	}
	
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
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isBeneficial() {
		return !isBadEffect();
	}
	
	public AlchemyPotion(String name, boolean isbad, int color) {
		this(name, isbad, color, false);
	}
	
	public AlchemyPotion(String name, boolean isbad, int color, boolean instant) {
		super(isbad, color);
		this.instant = instant;
		if (!isbad)
			setBeneficial();
		id = ++current_id;
		setPotionName("effect." + name);
		setRegistryName("potion_" + name);
		register();
	}
	
}
