package index.alchemy.core;

import java.util.Arrays;
import java.util.Map;

import org.apache.logging.log4j.LogManager;

import com.google.common.eventbus.EventBus;

import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.relauncher.IFMLCallHook;

import static index.alchemy.core.AlchemyConstants.*;

public class AlchemyModContainer extends DummyModContainer implements IFMLCallHook  {
	
	public AlchemyModContainer() {
		this(new ModMetadata());
		LogManager.getLogger(MOD_NAME).info("AlchemyModContainer -> new");
	}
	
	public AlchemyModContainer(ModMetadata metadata) {
		super(metadata);
		metadata.modId = "alchemy_core";
		metadata.name = "Alchemy-Core";
		metadata.version = "@core_version@";
		metadata.authorList = Arrays.asList("Mickeyxiami");
		metadata.description = "Alchemy mod core, as the pre-loading mod.";
		metadata.credits = "Mojang AB, and the Forge and FML guys. ";
	}
	
	@Override
	public boolean registerBus(EventBus bus, LoadController controller) {
		return true;
	}

	@Override
	public Void call() throws Exception {
		LogManager.getLogger(MOD_NAME).info("AlchemyModContainer -> call");
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) { }

}
