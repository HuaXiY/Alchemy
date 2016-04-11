package index.alchemy.potion;

import index.alchemy.core.AlchemyResourceLocation;
import index.alchemy.core.CommonProxy;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.IPlayerTickable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;

public class AlchemyPotion extends Potion {
	
	private boolean ready;
	
	@Override
	public boolean isReady(int tick, int level) {
        return ready;
    }
	
	@Override
	public boolean isInstant() {
        return ready;
        // test
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
		potionRegistry.register(-1, new AlchemyResourceLocation(name), this);
		if (this instanceof IPlayerTickable) 
			AlchemyEventSystem.registerPlayerTickable((IPlayerTickable) this);
	}
	
}
