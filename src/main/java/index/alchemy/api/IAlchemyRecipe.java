package index.alchemy.api;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IAlchemyRecipe {
	
	ResourceLocation getAlchemyName();
	
	default boolean matches(World world, BlockPos pos) { return true; }

	int getAlchemyTime();
	
	@SideOnly(Side.CLIENT)
	int getAlchemyColor();
	
	Fluid getAlchemyFluid();
	
	ItemStack getAlchemyResult(World world, BlockPos pos);
	
	List<IMaterialConsumer> getAlchemyMaterials();

}
