package index.alchemy.command;

import index.alchemy.api.annotation.Hook;
import index.alchemy.util.SideHelper;
import index.project.version.annotation.Omega;

import net.minecraft.client.entity.EntityPlayerSP;

import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Omega
@Hook.Provider
@SideOnly(Side.CLIENT)
public abstract class AlchemyCommandClient extends AlchemyCommand {
    
    public AlchemyCommandClient() {
        if (SideHelper.isClient())
            ClientCommandHandler.instance.registerCommand(this);
    }
    
    @Hook("net.minecraft.client.entity.EntityPlayerSP#func_70003_b")
    public static Hook.Result canUseCommand(EntityPlayerSP sender, int permLevel, String commandName) {
        return do_not_check_permission ? Hook.Result.TRUE : Hook.Result.VOID;
    }
    
}