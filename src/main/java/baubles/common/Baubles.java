package baubles.common;

import java.io.File;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
		modid = Baubles.MODID, 
		name = Baubles.MODNAME, 
		version = Baubles.VERSION, 
		dependencies="required-after:Forge@[12.17.0,);")
public class Baubles {
	
	public static final String MODID = "Baubles";
	public static final String MODNAME = "Baubles";
	public static final String VERSION = "1.2.1.0";

	
	@Instance(value=Baubles.MODID)
	public static Baubles instance;
	
	public File modDir;
	
	public static final Logger log = LogManager.getLogger("Baubles");
	public static final int GUI = 0;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		event.getModMetadata().version = Baubles.VERSION;
		modDir = event.getModConfigurationDirectory();

	}

	@EventHandler
	public void init(FMLInitializationEvent evt) {
		
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent evt) {
		
	}
		
}
