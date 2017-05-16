package index.alchemy.dlcs.exnails.core;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import index.alchemy.command.AlchemyCommandServer;
import index.project.version.annotation.Omega;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.server.MinecraftServer;

@Omega
public class ExFoodLoader {
	
	@Omega
	public static class CommandReloadFood extends AlchemyCommandServer {

		@Override
		public String getCommandName() {
			return "reload-food";
		}

		@Override
		public String getCommandUsage(ICommandSender sender) {
			return "/reload-food";
		}

		@Override
		public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
			try {
				loadConfig(ExNails.ITEM_FOOD_CFG);
			} catch (Exception e) { throw new CommandException(e.toString()); }
		}

	}
	
	private static final Logger logger = LogManager.getLogger(ExFoodLoader.class.getSimpleName());
	
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
						Item item = Item.getByNameOrId(args[0]);
						if (item != null) {
							if (item instanceof ItemFood) {
								ItemFood itemFood = (ItemFood) item;
								for (int i = 1, meta = 0; i < args.length; i += 2, meta++) {
									int heal = Integer.parseInt(args[i]);
									float saturationModifier = Float.parseFloat(args[i + 1]);
									itemFood.healAmount = heal;
									itemFood.saturationModifier = saturationModifier;
									logger.info("Set: " + args[0] + ":" + meta + " heal: " + heal + ", saturationModifier: " + saturationModifier);
								}
							} else
								logger.warn(item.getRegistryName() + " does not implement ItemFood");
						} else
							logger.info("Can not find: " + args[0]);
					} catch (Exception e) { logger.info("Can not parst: " + e.getMessage()); }
				}
			}
		});
	}

}
