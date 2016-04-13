package index.alchemy.core;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class AlchemyConfigLoader {
	
    private Configuration config;

    public AlchemyConfigLoader(File file)
    {
        config = new Configuration(file);
        config.load();
        initConfig();
        config.save();
    }

    private void initConfig() {}

}
