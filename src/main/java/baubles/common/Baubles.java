package baubles.common;

import index.project.version.annotation.Omega;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;

@Omega
@Mod(
		modid = Baubles.MODID, 
		name = Baubles.MODNAME, 
		version = Baubles.VERSION, 
		dependencies="required-after:Forge@[12.17.0,);"
)
public class Baubles {
	
	public static final String MODID = "Baubles";
	public static final String MODNAME = "Baubles";
	public static final String VERSION = "1.3.9";

	@Instance(Baubles.MODID)
	public static Baubles instance;
		
}