package index.alchemy.command;

import index.alchemy.api.annotation.Hook;
import index.alchemy.core.AlchemyModLoader;
import index.project.version.annotation.Omega;

import net.minecraft.entity.player.EntityPlayerMP;

import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Omega
@Hook.Provider
public abstract class AlchemyCommandServer extends AlchemyCommand {
    
    public AlchemyCommandServer() {
        AlchemyModLoader.addFMLEventCallback(FMLServerStartingEvent.class, e -> e.registerServerCommand(this));
    }
    
    @Hook("net.minecraft.entity.player.EntityPlayerMP#func_70003_b")
    public static Hook.Result canUseCommand(EntityPlayerMP sender, int permLevel, String commandName) {
        return do_not_check_permission ? Hook.Result.TRUE : Hook.Result.VOID;
    }
    
}
