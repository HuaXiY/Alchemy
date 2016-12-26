package index.alchemy.core;

import baubles.client.BaublesRenderLayer;
import index.alchemy.api.IMaterialContainer;
import index.alchemy.api.annotation.Hook;
import index.alchemy.entity.ai.EntityAIEatMeat;
import index.project.version.annotation.Omega;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Omega
@Hook.Provider
public class AlchemyHooks {
	
	@Hook(value = "biomesoplenty.common.remote.TrailManager#retrieveTrails", isStatic = true)
	public static final Hook.Result retrieveTrails() {
		return Hook.Result.NULL;
	}
	
	@SideOnly(Side.CLIENT)
	@Hook(value = "net.minecraft.client.renderer.entity.RenderPlayer#<init>", type = Hook.Type.TAIL)
	public static final void init_RenderPlayer(RenderPlayer renderPlayer, RenderManager renderManager, boolean useSmallArms) {
		renderPlayer.addLayer(new BaublesRenderLayer());
	}
	
	@Hook("net.minecraft.world.World#func_72875_a")
	public static final Hook.Result isMaterialInBB(World world, AxisAlignedBB bb, Material material) {
		int minX = MathHelper.floor_double(bb.minX);
		int maxX = MathHelper.ceiling_double_int(bb.maxX);
		int minY = MathHelper.floor_double(bb.minY);
		int maxY = MathHelper.ceiling_double_int(bb.maxY);
		int minZ = MathHelper.floor_double(bb.minZ);
		int maxZ = MathHelper.ceiling_double_int(bb.maxZ);
		BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain();
		for (int x = minX; x < maxX; x++)
			for (int y = minY; y < maxY; y++)
				for (int z = minZ; z < maxZ; z++) {
					Block block = world.getBlockState(pos.setPos(x, y, z)).getBlock();
					if (block instanceof IMaterialContainer && ((IMaterialContainer) block).isMaterialInBB(world, pos, material)) {
						pos.release();
						return Hook.Result.TRUE;
					}
				}
		pos.release();
		return Hook.Result.VOID;
	}
	
	@Hook(value = "net.minecraft.entity.passive.EntityWolf#func_184651_r", type = Hook.Type.TAIL)
	public static final void initEntityAI(EntityWolf wolf) {
		wolf.tasks.addTask(3, new EntityAIEatMeat(wolf));
	}
	
//	@Hook("net.minecraft.pathfinding.WalkNodeProcessor#getPathNodeTypeRaw")
//	public static final Hook.Result getPathNodeTypeRaw(WalkNodeProcessor processor, IBlockAccess access, int x, int y, int z) {
//		IBlockState state = access.getBlockState(new BlockPos(x, y, z));
//		if (state.getMaterial() == Material.WOOD && state.getBlock() instanceof BlockFenceGate)
//			return new Hook.Result(state.getValue(BlockFenceGate.OPEN) ? PathNodeType.DOOR_OPEN : PathNodeType.DOOR_WOOD_CLOSED);
//		return Hook.Result.VOID;
//	}
	
}
