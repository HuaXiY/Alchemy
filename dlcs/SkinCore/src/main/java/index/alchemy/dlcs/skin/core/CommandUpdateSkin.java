package index.alchemy.dlcs.skin.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import index.alchemy.command.AlchemyCommandClient;
import index.project.version.annotation.Omega;
import javafx.scene.image.Image;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

@Omega
public class CommandUpdateSkin extends AlchemyCommandClient {

	@Override
	public String getCommandName() {
		return "skin-update";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/skin-update [skin-type] [skin-url]";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		String type;
		File png;
		png = args.length < 2 ? new File(Minecraft.getMinecraft().mcDataDir, "skin.png") : 
			new File(args[1]);
		if (!png.exists() || png.isDirectory()) {
			sender.addChatMessage(new TextComponentString("Invalid path !!!"));
			return;
		}
		if (args.length > 0) {
			type = args[0].toLowerCase();
			if (!type.equals("slim") || !type.equals("default")) {
				sender.addChatMessage(new TextComponentString("Invalid type !!!"));
				return;
			}
		} else
			type = "default";
		try {
			Image image = new Image(new FileInputStream(png));
			if (SkinHelper.isSkin(image)) {
				if (SkinHelper.isX32(image))
					image = SkinHelper.x32Tox64(image);
				byte data[] = SkinHelper.toInputSteam(image);
				SkinCore.updateSkin(type, data, true);
			} else
				throw new IOException(png.toString());
		} catch (IOException e) {
			sender.addChatMessage(new TextComponentString("Invalid file !!!"));
			return;
		}
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return sender instanceof EntityPlayer;
	}

}
