package index.alchemy.api.event;

import javax.annotation.Nullable;

import index.alchemy.tile.TileEntityCauldron;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event.HasResult;

@HasResult
@Cancelable
public class CauldronActivatedEvent extends BlockEvent {
	
	public final EntityPlayer player;
	public final EnumHand hand;
	public final TileEntityCauldron cauldron;
	public final ItemStack heldItem;
	public final EnumFacing side;
	public final float hitX, hitY, hitZ;

	public CauldronActivatedEvent(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
			TileEntityCauldron cauldron, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		super(world, pos, state);
		this.player = player;
		this.hand = hand;
		this.cauldron = cauldron;
		this.heldItem = heldItem;
		this.side = side;
		this.hitX = hitX;
		this.hitY = hitY;
		this.hitZ = hitZ;
	}

}
