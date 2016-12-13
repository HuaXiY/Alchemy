package index.alchemy.tile;

import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.util.AABBHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AlchemyTileEntity extends TileEntity {
	
	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
		return oldState.getBlock() != newSate.getBlock();
	}
	
	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
	}
	
	public void updateTracker() {
		SPacketUpdateTileEntity packet = getUpdatePacket();
		for (EntityPlayerMP player : worldObj.getEntitiesWithinAABB(EntityPlayerMP.class, 
				AABBHelper.getAABBFromBlockPos(pos, AlchemyNetworkHandler.getTileEntityUpdateRange())))
			updatePlayer(player, packet);
	}
	
	public void updatePlayer(EntityPlayerMP player, SPacketUpdateTileEntity packet) {
		player.connection.sendPacket(packet);
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
		readFromNBT(packet.getNbtCompound());
	}

}
