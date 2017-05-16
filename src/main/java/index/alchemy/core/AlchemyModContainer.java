package index.alchemy.core;

import java.util.Arrays;

import com.google.common.eventbus.EventBus;

import index.project.version.annotation.Omega;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;

import static index.alchemy.core.AlchemyConstants.*;

@Omega
public class AlchemyModContainer extends DummyModContainer  {
	
	public AlchemyModContainer() { this(new ModMetadata()); }
	
	public AlchemyModContainer(ModMetadata metadata) {
		super(metadata);
		metadata.modId = CORE_MOD_ID;
		metadata.name = CORE_MOD_NAME;
		metadata.version = CORE_MOD_VERSION;
		metadata.authorList = Arrays.asList(AUTHORLIST);
		metadata.description = "Alchemy mod core, as the pre-loading mod.";
		metadata.credits = "Mojang AB, and the Forge and FML guys. ";
	}
	
	@Override
	public boolean registerBus(EventBus bus, LoadController controller) { return true; }

}
