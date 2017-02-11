package index.alchemy.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;

import index.alchemy.api.IRegister;
import index.alchemy.api.annotation.Config;
import index.alchemy.api.annotation.Hook;
import index.alchemy.api.event.PureDamageEvent;
import index.project.version.annotation.Beta;

@Beta
@Hook.Provider
public class AlchemyDamageSource extends DamageSource implements IRegister {
	
	public static final String CATEGORY_DAMAGE = "damage";
	
	@Config(category = CATEGORY_DAMAGE, comment = "Close will not cause the pure damage.")
	public static boolean ignore_pure_damage_source = false;
	
	protected boolean pure, dissipation;
	
	public <T extends AlchemyDamageSource> T setPureDamage() {
		pure = true;
		return (T) this;
	}
	
	public boolean isPureDamage() {
		return !ignore_pure_damage_source && pure;
	}
	
	public <T extends AlchemyDamageSource> T setDissipationDamage() {
		dissipation = true;
		return (T) this;
	}
	
	public boolean isDissipationDamage() {
		return dissipation;
	}
	
	public AlchemyDamageSource(String type) {
		super(type);
		register();
	}
	
	@Hook(value = "net.minecraftforge.common.ForgeHooks#onLivingAttack", isStatic = true)
	public static Hook.Result onLivingAttack(EntityLivingBase entity, DamageSource src, float amount) {
		if (src instanceof AlchemyDamageSource && ((AlchemyDamageSource) src).isPureDamage()) {
			PureDamageEvent event = new PureDamageEvent(entity, PureDamageEvent.Type.ATTACK, amount);
			return MinecraftForge.EVENT_BUS.post(event) ? Hook.Result.FALSE : Hook.Result.TRUE;
		}
		return Hook.Result.VOID;
	}
	
	@Hook(value = "net.minecraftforge.common.ForgeHooks#onLivingHurt", isStatic = true)
	public static Hook.Result onLivingHurt(EntityLivingBase entity, DamageSource src, float amount) {
		if (src instanceof AlchemyDamageSource && ((AlchemyDamageSource) src).isPureDamage()) {
			PureDamageEvent event = new PureDamageEvent(entity, PureDamageEvent.Type.HURT, amount);
			return MinecraftForge.EVENT_BUS.post(event) ? Hook.Result.ZERO : new Hook.Result(event.getAmount());
		}
		return Hook.Result.VOID;
	}
	
	@Hook(value = "net.minecraftforge.common.ForgeHooks#onLivingDeath", isStatic = true)
	public static Hook.Result onLivingDeath(EntityLivingBase entity, DamageSource src) {
		if (src instanceof AlchemyDamageSource && ((AlchemyDamageSource) src).isDissipationDamage()) {
			PureDamageEvent event = new PureDamageEvent(entity, PureDamageEvent.Type.DEATH, -1);
			return MinecraftForge.EVENT_BUS.post(event) ? Hook.Result.TRUE : Hook.Result.FALSE;
		}
		return Hook.Result.VOID;
	}
	
}
