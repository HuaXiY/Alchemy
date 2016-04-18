package index.alchemy.potion;

import index.alchemy.client.AlchemyResourceLocation;
import index.alchemy.core.CommonProxy;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.IEventHandle;
import index.alchemy.core.IPlayerTickable;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

public class AlchemyPotion extends Potion {
	
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
	
	@Override
	public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
		mc.getTextureManager().bindTexture(RESOURCE_LOCATION);
		mc.currentScreen.drawTexturedModalRect(x + 6, y + 6, id % 16 * 16, id / 16, 16, 16);
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
		REGISTRY.register(-1, new AlchemyResourceLocation(name), this);
		
		if (this instanceof IPlayerTickable) 
			AlchemyEventSystem.registerPlayerTickable((IPlayerTickable) this);
		
		if (this instanceof IEventHandle)
			AlchemyEventSystem.registerEventHandle((IEventHandle) this);
	}
	
}
