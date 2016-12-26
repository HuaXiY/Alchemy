package index.alchemy.client.fx.update;

import java.util.function.Consumer;
import java.util.function.Predicate;

import index.alchemy.api.IFXUpdate;
import index.alchemy.client.fx.AlchemyFX;
import index.project.version.annotation.Omega;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Omega
public class FXTriggerUpdate implements IFXUpdate {
	
	public Predicate<AlchemyFX> trigger;
	
	public FXTriggerUpdate(Predicate<AlchemyFX> trigger) {
		this.trigger = trigger;
	}
	
	@SafeVarargs
	public FXTriggerUpdate(Predicate<AlchemyFX>... triggers) {
		for (Predicate<AlchemyFX> trigger : triggers)
			this.trigger = this.trigger == null ? trigger : this.trigger.and(trigger);
	}
	
	@SafeVarargs
	public FXTriggerUpdate(Consumer<AlchemyFX> consumer, Predicate<AlchemyFX>... triggers) {
		this(triggers);
		Predicate<AlchemyFX> trigger = fx -> {
			consumer.accept(fx);
			return false;
		};
		this.trigger = this.trigger == null ? trigger : this.trigger.and(trigger);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean updateFX(AlchemyFX fx, long tick) {
		return trigger.test(fx);
	}
	
}
