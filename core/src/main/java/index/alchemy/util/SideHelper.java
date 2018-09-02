package index.alchemy.util;

import net.minecraftforge.fml.common.thread.SidedThreadGroup;
import net.minecraftforge.fml.common.thread.SidedThreadGroups;
import net.minecraftforge.fml.relauncher.Side;

public class SideHelper {
    
    private static boolean isClient = ASMHelper.getClassData("net.minecraft.client.Minecraft") != null;
    
    public static final boolean runOnClient() {
        return isClient;
    }
    
    public static final Side side() {
        final ThreadGroup group = Thread.currentThread().getThreadGroup();
        return group instanceof SidedThreadGroup ? ((SidedThreadGroup) group).getSide() : Side.CLIENT;
    }
    
    public static final SidedThreadGroup sideThreadGroup() {
        return mapSideThreadGroup(side());
    }
    
    public static final SidedThreadGroup mapSideThreadGroup(Side side) {
        return side.isClient() ? SidedThreadGroups.CLIENT : SidedThreadGroups.SERVER;
    }
    
    public static final boolean isServer() {
        return side().isServer();
    }
    
    public static final boolean isClient() {
        return side().isClient();
    }
    
}
