package index.alchemy.api.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class PureDamageEvent extends LivingEvent {
	
	public enum Type {
		ATTACK, HURT, DEATH
	}
	
	public final Type type;
	protected float amount;
	
	public float getAmount() {
		return amount;
	}
	
	public void setAmount(float amount) {
		this.amount = amount;
	}
	
	public PureDamageEvent(EntityLivingBase entity, Type type, float amount) {
		super(entity);
		this.type = type;
		this.amount = amount;
	}

}
