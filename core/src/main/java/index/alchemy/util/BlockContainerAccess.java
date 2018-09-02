package index.alchemy.util;

import index.project.version.annotation.Omega;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

@Omega
public class BlockContainerAccess extends FakeBlockAccess {
    
    public final BlockPos pos;
    public final IBlockState state;
    
    public BlockContainerAccess(IBlockAccess access, BlockPos pos, IBlockState state) {
        super(access);
        this.pos = pos;
        this.state = state;
    }
    
    @Override
    public IBlockState getBlockState(BlockPos pos) {
        return this.pos.equals(pos) ? state : super.getBlockState(pos);
    }
    
}
