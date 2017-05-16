package index.alchemy.dlcs.exnails.core;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import index.alchemy.api.IBlockTemperature;
import index.alchemy.command.AlchemyCommandServer;
import index.project.version.annotation.Omega;
import net.minecraft.block.Block;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

@Omega
public class ExTemperatureLoader {
	
	@Omega
	public static class CommandReloadTemperature extends AlchemyCommandServer {

		@Override
		public String getCommandName() {
			return "reload-temperature";
		}

		@Override
		public String getCommandUsage(ICommandSender sender) {
			return "/reload-temperature";
		}

		@Override
		public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
			try {
				loadConfig(ExNails.BLOCK_TEMPERATURE_CFG);
			} catch (Exception e) { throw new CommandException(e.toString()); }
		}

	}
	
	private static final Logger logger = LogManager.getLogger(ExTemperatureLoader.class.getSimpleName());
	
	public static void loadConfig(File file) throws IOException {
		Files.readLines(file, Charsets.UTF_8).forEach(s -> {
			int index = s.indexOf('#');
			if (index != -1)
				s = s.substring(0, index);
			if (!s.isEmpty()) {
				s = s.replace("\\ ", "$blank$");
				String args[] = s.split(" ");
				args[0] = args[0].replace("$blank$", " ");
				if (args.length > 2 && args.length % 2 == 1) {
					try {
						Block block = Block.getBlockFromName(args[0]);
						if (block != null) {
							if (block instanceof IBlockTemperature) {
								IBlockTemperature blockTemperature = (IBlockTemperature) block;
								for (int i = 1, meta = 0; i < args.length; i++, meta++) {
									float temperature = Float.parseFloat(args[i]);
									blockTemperature.setBlockTemperature(block.getStateFromMeta(meta), temperature);
									logger.info("Set: " + args[0] + ":" + meta + " temperature: " + temperature);
								}
							} else
								logger.warn(block.getRegistryName() + " does not implement IBlockTemperature");
						} else
							logger.info("Can not find: " + args[0]);
					} catch (Exception e) { logger.info("Can not parst: " + e.getMessage()); }
				}
			}
		});
	}

}
