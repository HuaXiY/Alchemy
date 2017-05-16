package index.alchemy.dlcs.non_update.core;

import java.io.File;

import index.project.version.annotation.Omega;
import net.minecraftforge.common.config.Configuration;

@Omega
public class Config {
	
	private static Configuration configuration = new Configuration(new File("config/" + NonUpdate.DLC_NAME + ".cfg"));
	
	public static void init() {
		configuration.load();
		loadConfiguration();
		configuration.save();
	}
	
	static boolean onlyPreventMainThread = false;
	static String redirectAddress = "127.0.0.1";
	
	private static void loadConfiguration() {
		onlyPreventMainThread = configuration.getBoolean("onlyPreventMainThread", "Thread", onlyPreventMainThread, "Only prevent main thread url access.");
		redirectAddress = configuration.getString("redirectAddress", "Address", redirectAddress, "Redirect address.");
	}

}
