package index.alchemy.tile;

import java.io.IOException;
import java.util.Random;

import index.alchemy.api.annotation.Patch;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.util.AABBHelper;
import index.project.version.annotation.Omega;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketBlockAction;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Omega
public class AlchemyTileEntity extends TileEntity {
    
    public static final Random random = new Random(); 
    
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
    	if (worldObj != null) {
	        SPacketUpdateTileEntity packet = getUpdatePacket();
	        for (EntityPlayerMP player : worldObj.getEntitiesWithinAABB(EntityPlayerMP.class, 
	                AABBHelper.getAABBFromBlockPos(pos, AlchemyNetworkHandler.getTileEntityUpdateRange())))
	            updatePlayer(player, packet);
    	}
    }
    
    public void updatePlayer(EntityPlayerMP player, SPacketUpdateTileEntity packet) {
        player.connection.sendPacket(packet);
    }
    
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        readFromNBT(packet.getNbtCompound());
    }
    
    @Patch("net.minecraft.network.play.server.SPacketBlockAction")
    public static class Patch$SPacketBlockAction extends SPacketBlockAction {
        
        @Override
        public void writePacketData(PacketBuffer buf) throws IOException {
            buf.writeBlockPos(blockPosition);
            buf.writeByte(instrument);
            buf.writeInt(pitch);
            buf.writeVarIntToBuffer(Block.getIdFromBlock(block));
        }
        
        @Override
        public void readPacketData(PacketBuffer buf) throws IOException {
            blockPosition = buf.readBlockPos();
            instrument = buf.readUnsignedByte();
            pitch = buf.readInt();
            block = Block.getBlockById(buf.readVarIntFromBuffer());
        }
        
    }

}
