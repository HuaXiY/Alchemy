package index.alchemy.interacting.minecraft;

import index.alchemy.api.annotation.Patch;
import index.alchemy.api.event.PlayerDropItemEvent;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;

@Patch("net.minecraft.network.NetHandlerPlayServer")
public class Patch$NetHandlerPlayServer extends NetHandlerPlayServer {
	
	@Patch.Exception
	public Patch$NetHandlerPlayServer(MinecraftServer server, NetworkManager networkManagerIn, EntityPlayerMP playerIn) {
		super(server, networkManagerIn, playerIn);
	}

	@Override
	public void processPlayerDigging(CPacketPlayerDigging packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, player.getServerWorld());
        WorldServer worldserver = server.getWorld(player.dimension);
        BlockPos blockpos = packetIn.getPosition();
        player.markPlayerActive();
        switch (packetIn.getAction()) {
            case SWAP_HELD_ITEMS:
                if (!player.isSpectator()) {
                    ItemStack itemstack = player.getHeldItem(EnumHand.OFF_HAND);
                    player.setHeldItem(EnumHand.OFF_HAND, player.getHeldItem(EnumHand.MAIN_HAND));
                    player.setHeldItem(EnumHand.MAIN_HAND, itemstack);
                }
                return;
            case DROP_ITEM:
                if (!player.isSpectator() && !MinecraftForge.EVENT_BUS.post(new PlayerDropItemEvent(player, player.inventory.getCurrentItem(), false)))
                    player.dropItem(false);
                return;
            case DROP_ALL_ITEMS:
                if (!player.isSpectator() && !MinecraftForge.EVENT_BUS.post(new PlayerDropItemEvent(player, player.inventory.getCurrentItem(), true)))
                    player.dropItem(true);
                return;
            case RELEASE_USE_ITEM:
                player.stopActiveHand();
                return;
            case START_DESTROY_BLOCK:
            case ABORT_DESTROY_BLOCK:
            case STOP_DESTROY_BLOCK:
                double d0 = player.posX - ((double)blockpos.getX() + 0.5D);
                double d1 = player.posY - ((double)blockpos.getY() + 0.5D) + 1.5D;
                double d2 = player.posZ - ((double)blockpos.getZ() + 0.5D);
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;
                double dist = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue() + 1;
                dist *= dist;
                if (d3 > dist)
                    return;
                else if (blockpos.getY() >= server.getBuildLimit())
                    return;
                else
                {
                    if (packetIn.getAction() == CPacketPlayerDigging.Action.START_DESTROY_BLOCK)
                        if (!server.isBlockProtected(worldserver, blockpos, player) && worldserver.getWorldBorder().contains(blockpos))
                            player.interactionManager.onBlockClicked(blockpos, packetIn.getFacing());
                        else
                            player.connection.sendPacket(new SPacketBlockChange(worldserver, blockpos));
                    else {
                        if (packetIn.getAction() == CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK)
                            player.interactionManager.blockRemoving(blockpos);
                        else if (packetIn.getAction() == CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK)
                            player.interactionManager.cancelDestroyingBlock();
                        if (worldserver.getBlockState(blockpos).getMaterial() != Material.AIR)
                            player.connection.sendPacket(new SPacketBlockChange(worldserver, blockpos));
                    }
                    return;
                }
            default:
                throw new IllegalArgumentException("Invalid player action");
        }
	}

}
