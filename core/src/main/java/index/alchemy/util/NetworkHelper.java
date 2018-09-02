package index.alchemy.util;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public interface NetworkHelper {
    
    static boolean isRemoteGaming() {
        FMLCommonHandler handler = FMLCommonHandler.instance();
        return (handler.getEffectiveSide() == Side.SERVER || handler.getClientToServerNetworkManager() != null) && handler.getMinecraftServerInstance() == null;
    }
    
}
